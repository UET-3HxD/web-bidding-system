package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.Auction;
import com.auction.team3HxD.model.Item;
import com.auction.team3HxD.model.User;
import com.auction.team3HxD.model.enums.AuctionStatus;
import com.auction.team3HxD.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {
    private final UserDAO userDAO = new UserDAO();
    private final ItemDAO itemDAO = new ItemDAO();

    public boolean startAuction(int itemId, int durationMinutes) {
        String insertSessionSql = "INSERT INTO auction_sessions (item_id, start_time, end_time, current_price, status) " +
                "VALUES (?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 0, 'ACTIVE')";
        String updateItemSql = "UPDATE items SET status = 'LIVE' WHERE id = ?";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertSessionSql)) {
                pstmt1.setInt(1, itemId);
                pstmt1.setInt(2, durationMinutes);
                pstmt1.executeUpdate();
            }

            try (PreparedStatement pstmt2 = conn.prepareStatement(updateItemSql)) {
                pstmt2.setInt(1, itemId);
                pstmt2.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println(">>> startAuction ERROR: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<Auction> findLiveAuctions() {
        List<Auction> list = new ArrayList<>();

        String sql = "SELECT a.*, (SELECT COUNT(*) FROM bids b WHERE b.auction_id = a.id) AS bid_count " +
                "FROM auction_sessions a " +
                "WHERE a.status = 'ACTIVE' AND a.end_time > NOW() " +
                "ORDER BY a.end_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Auction auction = mapResultSetToAuction(rs);
                auction.setBidCount(rs.getInt("bid_count"));
                list.add(auction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public String placeBidTransaction(int auctionId, int userId, double bidAmount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // SỬA: Dùng JOIN thay vì subquery để tránh lỗi forward reference
            String checkSql = "SELECT a.current_price, a.status, a.end_time, i.bid_increment " +
                    "FROM auction_sessions a " +
                    "JOIN items i ON a.item_id = i.id " +
                    "WHERE a.id = ? FOR UPDATE";

            double currentPrice = 0;
            double increment = 0;

            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, auctionId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if (!"ACTIVE".equals(status)) {
                            conn.rollback();
                            return "ERROR|Phiên đấu giá đã kết thúc!";
                        }
                        currentPrice = rs.getDouble("current_price");
                        increment = rs.getDouble("bid_increment");
                    } else {
                        conn.rollback();
                        return "ERROR|Không tìm thấy phiên đấu giá!";
                    }
                }
            }

            if (bidAmount < (currentPrice + increment)) {
                conn.rollback();
                return "ERROR|Giá đặt phải lớn hơn giá hiện tại cộng bước giá!";
            }

            String insertBidSql = "INSERT INTO bids (auction_id, user_id, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement psInsert = conn.prepareStatement(insertBidSql)) {
                psInsert.setInt(1, auctionId);
                psInsert.setInt(2, userId);
                psInsert.setDouble(3, bidAmount);
                psInsert.executeUpdate();
            }

            String updateAuctionSql = "UPDATE auction_sessions SET current_price = ? WHERE id = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateAuctionSql)) {
                psUpdate.setDouble(1, bidAmount);
                psUpdate.setInt(2, auctionId);
                psUpdate.executeUpdate();
            }

            conn.commit();

            User bidder = userDAO.findById(userId);
            String bidderName = (bidder != null) ? bidder.getUsername() : "Unknown";

            return "SUCCESS|" + bidAmount + "|" + bidderName;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "ERROR|Lỗi hệ thống khi xử lý giao dịch!";
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Auction findById(int id) {
        String sql = "SELECT * FROM auction_sessions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuction(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm Auction theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String getHighestBidderName(int auctionId) {
        String sql = "SELECT u.username FROM bids b " +
                "JOIN users u ON b.user_id = u.id " +
                "WHERE b.auction_id = ? ORDER BY b.bid_amount DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public double getUserLastBid(int auctionId, int userId) {
        String sql = "SELECT MAX(bid_amount) FROM bids WHERE auction_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int itemId = rs.getInt("item_id");

        double currentPrice = rs.getDouble("current_price");
        if (rs.wasNull()) currentPrice = 0;

        AuctionStatus status = AuctionStatus.valueOf(rs.getString("status"));

        LocalDateTime startTime = rs.getTimestamp("start_time") != null ?
                rs.getTimestamp("start_time").toLocalDateTime() : LocalDateTime.now();
        LocalDateTime endTime = rs.getTimestamp("end_time") != null ?
                rs.getTimestamp("end_time").toLocalDateTime() : LocalDateTime.now().plusHours(1);

        Item item = itemDAO.findById(itemId);

        User seller = null;
        double bidIncrement = 0;
        if (item != null) {
            seller = userDAO.findById(item.getSellerId());
            // Tính bidIncrement = 2% giá khởi điểm (giống AuctionService)
            bidIncrement = item.getPrice() * 0.02;
        }

        double startPrice = (item != null) ? item.getPrice() : 0.0;

        Auction auction = new Auction(
                id, seller, item, startPrice, currentPrice, bidIncrement,
                status, startTime, endTime
        );

        if (auction.getStatus() == AuctionStatus.ACTIVE && LocalDateTime.now().isAfter(auction.getEndTime())) {
            auction.endAuction();
        }
        return auction;
    }
}