package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.*;
import com.auction.team3HxD.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidDAO {

    private final UserDAO userDAO = new UserDAO();
    private final AuctionDAO auctionDAO = new AuctionDAO();

    // ================= INSERT BID =================
    public int insert(BidTransaction bid) {
        String sql = """
            INSERT INTO bids (auction_id, bidder_id, amount, created_at)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {

            //  Transaction cực quan trọng
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, bid.getAuction().getId());
                ps.setInt(2, bid.getBidder().getId());
                ps.setDouble(3, bid.getAmount());
                ps.setTimestamp(4, Timestamp.valueOf(bid.getTimestamp()));

                ps.executeUpdate();

                //  update current_price của auction
                updateAuctionPrice(conn, bid);

                conn.commit();

                // lấy id vừa insert
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        bid.setId(id);
                        return id;
                    }
                }

            } catch (Exception e) {
                conn.rollback(); // rollback nếu lỗi
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException("Insert bid failed", e);
        }

        return -1;
    }

    // ================= FIND ALL BY AUCTION =================
    public List<BidTransaction> findByAuction(int auctionId) {
        List<BidTransaction> list = new ArrayList<>();

        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY amount DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auctionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find bids by auction failed", e);
        }

        return list;
    }

    // ================= FIND HIGHEST BID =================
    public BidTransaction findHighestBid(int auctionId) {
        String sql = """
            SELECT * FROM bids
            WHERE auction_id = ?
            ORDER BY amount DESC
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auctionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find highest bid failed", e);
        }

        return null;
    }

    // ================= FIND BY USER =================
    public List<BidTransaction> findByUser(int userId) {
        List<BidTransaction> list = new ArrayList<>();

        String sql = "SELECT * FROM bids WHERE bidder_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find bids by user failed", e);
        }

        return list;
    }

    // ================= DELETE =================
    public void delete(int id) {
        String sql = "DELETE FROM bids WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Delete bid failed", e);
        }
    }

    // ================= HELPER =================
    private BidTransaction mapResultSet(ResultSet rs) throws SQLException {

        int id = rs.getInt("id");

        int auctionId = rs.getInt("auction_id");
        Auction auction = auctionDAO.findById(auctionId);

        int bidderId = rs.getInt("bidder_id");
        Bidder bidder = (Bidder) userDAO.findById(bidderId);

        double amount = rs.getDouble("amount");

        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

        BidTransaction bid = new BidTransaction(auction, (Bidder) bidder, amount);
        bid.setId(id);
        bid.setTimestamp(createdAt);
        return bid;
    }

    // ================= UPDATE AUCTION PRICE =================
    private void updateAuctionPrice(Connection conn, BidTransaction bid) throws SQLException {
        String sql = "UPDATE auctions SET current_price = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, bid.getAmount());
            ps.setInt(2, bid.getAuction().getId());
            ps.executeUpdate();
        }
    }
}