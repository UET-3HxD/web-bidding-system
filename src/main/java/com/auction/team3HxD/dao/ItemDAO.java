package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.*;
import com.auction.team3HxD.util.DBConnection;

import java.sql.*;
import java.util.UUID;

public class ItemDAO {

    private final UserDAO userDAO = new UserDAO();

    public Item findById(UUID id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        Item item = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    item = mapResultSetToItem(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }

    // ===== MAPPING =====
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {

        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startingPrice = rs.getDouble("starting_price");
        String imageURL = rs.getString("image_url");

        int ownerId = rs.getInt("owner_id");
        Seller owner = (Seller) userDAO.findById(ownerId);

        return new NormalItem(id, name, description, startingPrice, imageURL, owner);
    }
}