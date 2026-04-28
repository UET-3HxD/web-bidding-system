package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.*;
import com.auction.team3HxD.model.enums.AuctionStatus;
import com.auction.team3HxD.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionDAO {

    // Dùng lại UserDAO để lấy Seller
    private final UserDAO userDAO = new UserDAO();

    // ================= CREATE =================
    // Thêm auction mới vào DB
    public void insert(Auction auction) {
        String sql = "INSERT INTO auctions " +
                "(id, seller_id, item_id, start_price, current_price, bid_increment, status, start_time, end_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, auction.getId().toString());
            ps.setString(2, auction.getSeller().getId().toString());
            ps.setString(3, auction.getItem().getId().toString());

            ps.setDouble(4, auction.getStartPrice());
            ps.setDouble(5, auction.getCurrentPrice());
            ps.setDouble(6, auction.getBidIncrement());

            ps.setString(7, auction.getStatus().name());
            ps.setTimestamp(8, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(9, Timestamp.valueOf(auction.getEndTime()));

            ps.executeUpdate();
            System.out.println("Insert Auction thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= READ ALL =================
    // Lấy toàn bộ auction
    public List<Auction> findAll() {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Auction auction = mapResultSetToAuction(rs);
                list.add(auction);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= READ BY ID =================
    // Tìm auction theo id
    public Auction findById(UUID id) {
        String sql = "SELECT * FROM auctions WHERE id = ?";
        Auction auction = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    auction = mapResultSetToAuction(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auction;
    }

    // ================= UPDATE =================
    // Cập nhật auction
    public void update(Auction auction) {
        String sql = "UPDATE auctions SET " +
                "seller_id=?, item_id=?, start_price=?, current_price=?, bid_increment=?, status=?, start_time=?, end_time=? " +
                "WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, auction.getSeller().getId().toString());
            ps.setString(2, auction.getItem().getId().toString());

            ps.setDouble(3, auction.getStartPrice());
            ps.setDouble(4, auction.getCurrentPrice());
            ps.setDouble(5, auction.getBidIncrement());

            ps.setString(6, auction.getStatus().name());
            ps.setTimestamp(7, Timestamp.valueOf(auction.getStartTime()));
            ps.setTimestamp(8, Timestamp.valueOf(auction.getEndTime()));

            ps.setString(9, auction.getId().toString());

            ps.executeUpdate();
            System.out.println("Update Auction thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= DELETE =================
    // Xoá auction
    public void delete(UUID id) {
        String sql = "DELETE FROM auctions WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();

            System.out.println("Delete Auction thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FIND BY SELLER =================
    // Lấy tất cả auction của 1 seller
    public List<Auction> findBySeller(UUID sellerId) {
        List<Auction> list = new ArrayList<>();
        String sql = "SELECT * FROM auctions WHERE seller_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sellerId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAuction(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ================= HELPER =================
    // Convert ResultSet -> Auction object
    private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {

        UUID id = UUID.fromString(rs.getString("id"));

        // ===== Seller =====
        UUID sellerId = UUID.fromString(rs.getString("seller_id"));
        Seller seller = (Seller) userDAO.findById(sellerId);

        // ===== Item =====
        ItemDAO itemDAO = new ItemDAO();

        UUID itemId = UUID.fromString(rs.getString("item_id"));
        Item item = itemDAO.findById(itemId);

        // ===== Data =====
        double startPrice = rs.getDouble("start_price");
        double currentPrice = rs.getDouble("current_price");
        double bidIncrement = rs.getDouble("bid_increment");

        AuctionStatus status = AuctionStatus.valueOf(rs.getString("status"));

        LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
        LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();

        // ===== Tạo object =====
        Auction auction = new Auction(
                id, seller, item,
                startPrice, currentPrice, bidIncrement,
                status, startTime, endTime
        );

        return auction;
    }
}