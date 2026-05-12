package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.*;
import com.auction.team3HxD.model.enums.AuctionStatus;
import com.auction.team3HxD.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    private final UserDAO userDAO = new UserDAO();
    private final ItemDAO itemDAO = new ItemDAO();

    // ================= INSERT =================
    public int insert(Auction auction) {
        String sql = """
            INSERT INTO auctions 
            (seller_id, item_id, start_price, current_price, bid_increment, status, start_time, end_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, auction.getSeller().getId());
            ps.setInt(2, auction.getItem().getId());

            ps.setDouble(3, auction.getStartPrice());
            ps.setDouble(4, auction.getCurrentPrice());
            ps.setDouble(5, auction.getBidIncrement());

            ps.setString(6, auction.getStatus().name());
            ps.setTimestamp(7, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(8, Timestamp.valueOf(auction.getEndTime()));

            ps.executeUpdate();

            // lấy ID auto increment
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    auction.setId(id);
                    return id;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Insert auction failed", e);
        }

        return -1;
    }

    // ================= FIND ALL =================
    public List<Auction> findAll() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find all failed", e);
        }

        return list;
    }

    // ================= FIND BY ID =================
    public Auction findById(int id) {
        String sql = "SELECT * FROM auctions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find by id failed", e);
        }

        return null;
    }

    // ================= UPDATE =================
    public void update(Auction auction) {
        String sql = """
            UPDATE auctions SET 
                seller_id = ?, 
                item_id = ?, 
                start_price = ?, 
                current_price = ?, 
                bid_increment = ?, 
                status = ?, 
                start_time = ?, 
                end_time = ?
            WHERE id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auction.getSeller().getId());
            ps.setInt(2, auction.getItem().getId());

            ps.setDouble(3, auction.getStartPrice());
            ps.setDouble(4, auction.getCurrentPrice());
            ps.setDouble(5, auction.getBidIncrement());

            ps.setString(6, auction.getStatus().name());
            ps.setTimestamp(7, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(8, Timestamp.valueOf(auction.getEndTime()));

            ps.setInt(9, auction.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Update failed", e);
        }
    }

    // ================= DELETE =================
    public void delete(int id) {
        String sql = "DELETE FROM auctions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Delete failed", e);
        }
    }

    // ================= FIND BY SELLER =================
    public List<Auction> findBySeller(int sellerId) {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE seller_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sellerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find by seller failed", e);
        }

        return list;
    }

    // ================= HELPER =================
    private Auction mapResultSet(ResultSet rs) throws SQLException {

        int id = rs.getInt("id");

        int sellerId = rs.getInt("seller_id");
        Seller seller = (Seller) userDAO.findById(sellerId);

        int itemId = rs.getInt("item_id");
        Item item = itemDAO.findById(itemId);

        double startPrice = rs.getDouble("start_price");
        double currentPrice = rs.getDouble("current_price");
        double bidIncrement = rs.getDouble("bid_increment");

        AuctionStatus status = AuctionStatus.valueOf(rs.getString("status"));

        LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
        LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

        return new Auction(
                id, seller, item,
                startPrice, currentPrice, bidIncrement,
                status, startTime, endTime
        );
    }
}