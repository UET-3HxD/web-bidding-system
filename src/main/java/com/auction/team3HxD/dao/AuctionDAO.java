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
        // Lệnh 1: Tạo phiên đấu giá, sử dụng DATE_ADD của MySQL để cộng phút vào giờ hiện tại
        String insertSessionSql = "INSERT INTO auction_sessions (item_id, end_time, status) " +
                "VALUES (?, DATE_ADD(NOW(), INTERVAL ? MINUTE), 'ACTIVE')";
        // Lệnh 2: Cập nhật trạng thái sản phẩm
        String updateItemSql = "UPDATE items SET status = 'LIVE' WHERE id = ?";

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            // TẮT AUTO-COMMIT để bắt đầu một Transaction
            conn.setAutoCommit(false);

            // Bước 1: Thực thi tạo phiên
            try (PreparedStatement pstmt1 = conn.prepareStatement(insertSessionSql)) {
                pstmt1.setInt(1, itemId);
                pstmt1.setInt(2, durationMinutes);
                pstmt1.executeUpdate();
            }

            // Bước 2: Thực thi cập nhật item
            try (PreparedStatement pstmt2 = conn.prepareStatement(updateItemSql)) {
                pstmt2.setInt(1, itemId);
                pstmt2.executeUpdate();
            }

            // Nếu cả 2 bước đều chạy mượt mà -> COMMIT lưu vào Database
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println(">>> Lỗi khi tạo phiên đấu giá: " + e.getMessage());
            // CÓ LỖI -> ROLLBACK để khôi phục lại trạng thái ban đầu
            if (conn != null) {
                try { conn.rollback(); }
                catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            // Luôn nhớ trả lại trạng thái auto-commit cho Connection Pool
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                }
                catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
    public List<Auction> findLiveAuctions() {
        List<Auction> list = new ArrayList<>();

        // Câu truy vấn vẫn JOIN để lấy thông tin Item và đếm Bid cho tối ưu
        String sql = "SELECT a.*, (SELECT COUNT(*) FROM bids b WHERE b.auction_id = a.id) AS bid_count " +
                "FROM auction_sessions a " +
                "WHERE a.status = 'ACTIVE' AND a.end_time > NOW() " +
                "ORDER BY a.end_time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Sử dụng lại hàm map mà bạn DAO đã viết
                Auction auction = mapResultSetToAuction(rs);

                // Gán thêm số lượt bid vừa đếm được
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
            // 1. Tắt AutoCommit để bắt đầu một Transaction (Giao dịch)
            conn.setAutoCommit(false);

            // 2. KHÓA DÒNG (Row Lock): Lấy thông tin phiên đấu giá và khóa nó lại
            // Lệnh FOR UPDATE đảm bảo không luồng nào khác được sửa dòng này cho đến khi Transaction kết thúc
            String checkSql = "SELECT current_price, status, end_time, " +
                    "(SELECT bid_increment FROM items WHERE id = a.item_id) as bid_increment " +
                    "FROM auction_sessions a WHERE id = ? FOR UPDATE";

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

            // 3. Kiểm tra logic giá (Bước nhảy)
            if (bidAmount < (currentPrice + increment)) {
                conn.rollback();
                return "ERROR|Giá đặt phải lớn hơn giá hiện tại cộng bước giá!";
            }

            // 4. Ghi nhận lịch sử đặt giá vào bảng bids
            String insertBidSql = "INSERT INTO bids (auction_id, user_id, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";
            try (PreparedStatement psInsert = conn.prepareStatement(insertBidSql)) {
                psInsert.setInt(1, auctionId);
                psInsert.setInt(2, userId);
                psInsert.setDouble(3, bidAmount);
                psInsert.executeUpdate();
            }

            // 5. Cập nhật giá mới nhất lên bảng auction_sessions
            String updateAuctionSql = "UPDATE auction_sessions SET current_price = ? WHERE id = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateAuctionSql)) {
                psUpdate.setDouble(1, bidAmount);
                psUpdate.setInt(2, auctionId);
                psUpdate.executeUpdate();
            }

            // 6. Hoàn tất giao dịch (Lưu vào DB và mở khóa)
            conn.commit();

            // Lấy tên người vừa bid để trả về (Dùng cho thông báo)
            User bidder = userDAO.findById(userId);
            String bidderName = (bidder != null) ? bidder.getUsername() : "Unknown";

            return "SUCCESS|" + bidAmount + "|" + bidderName;

        } catch (SQLException e) {
            // Cấp cứu: Nếu có bất kỳ lỗi gì (mất mạng, lỗi SQL), hoàn tác toàn bộ!
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return "ERROR|Lỗi hệ thống khi xử lý giao dịch!";
        } finally {
            // Trả lại trạng thái mặc định cho Connection
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
        // 1. Lấy thông tin cơ bản của Session
        int id = rs.getInt("id");
        int itemId = rs.getInt("item_id");
        double currentPrice = rs.getDouble("current_price");
        double bidIncrement = rs.getDouble("bid_increment");

        // Ánh xạ Enum từ String trong DB
        com.auction.team3HxD.model.enums.AuctionStatus status =
                com.auction.team3HxD.model.enums.AuctionStatus.valueOf(rs.getString("status"));

        // Chuyển đổi Timestamp sang LocalDateTime để khớp với Model
        java.time.LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
        java.time.LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

        // 2. Sử dụng ItemDAO để lấy đối tượng Item đầy đủ
        // Điều này đảm bảo chúng ta có Item.getName(), Item.getPrice(),...
        Item item = itemDAO.findById(itemId);

        // 3. Lấy thông tin Seller từ Item
        // Vì bảng items đã lưu seller_id, chúng ta lấy ra để tạo đối tượng User (Seller)
        User seller = null;
        if (item != null) {
            seller = userDAO.findById(item.getSellerId());
        }

        // 4. Lấy giá khởi điểm từ Item
        double startPrice = (item != null) ? item.getPrice() : 0.0;

        // 5. Khởi tạo đối tượng Auction bằng Constructor 2 mà Captain đã cung cấp
        Auction auction = new Auction(
                id,
                seller,
                item,
                startPrice,
                currentPrice,
                bidIncrement,
                status,
                startTime,
                endTime
        );
        if (auction.getStatus() == AuctionStatus.ACTIVE && LocalDateTime.now().isAfter(auction.getEndTime())) {
            auction.endAuction();
        }
        return auction;
    }
}
