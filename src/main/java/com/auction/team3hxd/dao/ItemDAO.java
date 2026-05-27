package com.auction.team3hxd.dao;

import com.auction.team3hxd.model.*;
import com.auction.team3hxd.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    private final UserDAO userDAO = new UserDAO();

    public Item findById(int id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int saveItem(Item item, String type) {
        String sql =
                "INSERT INTO items (seller_id, product_name, starting_price, description, image_path, item_type) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, item.getSellerId());
            pstmt.setString(2, item.getName());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setString(4, item.getDescription());
            pstmt.setString(5, item.getImagePath());
            pstmt.setString(6, type.toUpperCase());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(">>> Lỗi SQL khi lưu Item: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public List<Item> getAllItemsBySeller(int sellerId) {
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ? " +
                "AND status IN ('APPROVED', 'WAITING', 'REJECTED', 'LIVE', 'SOLD') " +
                "ORDER BY CASE status " +
                "  WHEN 'APPROVED' THEN 1 " +
                "  WHEN 'WAITING' THEN 2 " +
                "  WHEN 'REJECTED' THEN 3 " +
                "  WHEN 'LIVE' THEN 4 " +
                "  WHEN 'SOLD' THEN 5 " +
                "  ELSE 6 END, id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sellerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String type = rs.getString("item_type");
                Item item = null;

                int id = rs.getInt("id");
                String name = rs.getString("product_name");
                String desc = rs.getString("description");
                double price = rs.getDouble("starting_price");
                String path = rs.getString("image_path");
                String status = rs.getString("status");
                LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);

                switch (type) {
                    case "ELECTRONIC":
                        item = new Electronic(id, sellerId, name, desc, price, path, status, createdAt);
                        break;
                    case "ART":
                        item = new Art(id, sellerId, name, desc, price, path, status, createdAt);
                        break;
                    case "VEHICLE":
                        item = new Vehicle(id, sellerId, name, desc, price, path, status, createdAt);
                        break;
                    case "OTHER":
                        item = new Electronic(id, sellerId, name, desc, price, path, status, createdAt);
                        break;
                }
                if (item != null) {
                    itemList.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(">>> [LỖI TRUY VẤN] Lỗi khi lấy danh sách sản phẩm:");
        }
        return itemList;
    }

    public int updateItemInfo(int itemId, String name, double price, String desc) {
        String sql =
                "UPDATE items SET product_name = ?, starting_price = ?, description = ?, status = 'WAITING' "
                        +
                        "WHERE id = ? AND status NOT IN ('LIVE', 'SOLD')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setString(3, desc);
            pstmt.setInt(4, itemId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return itemId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getOwnerIdByItemId(int itemId) {
        String sql = "SELECT seller_id FROM items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("seller_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE id = ? AND status NOT IN ('LIVE', 'SOLD')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== ADMIN: Lấy sản phẩm theo trạng thái ====================
    public List<Item> getItemsByStatus(String status) {
        List<Item> list = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE status = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String type = rs.getString("item_type");
                Item item = null;

                int id = rs.getInt("id");
                int sellerId = rs.getInt("seller_id");
                String name = rs.getString("product_name");
                String desc = rs.getString("description");
                double price = rs.getDouble("starting_price");
                String path = rs.getString("image_path");
                String itemStatus = rs.getString("status");
                LocalDateTime createdAt = rs.getObject("created_at", LocalDateTime.class);

                switch (type != null ? type : "ELECTRONIC") {
                    case "ELECTRONIC":
                        item = new Electronic(id, sellerId, name, desc, price, path, itemStatus, createdAt);
                        break;
                    case "ART":
                        item = new Art(id, sellerId, name, desc, price, path, itemStatus, createdAt);
                        break;
                    case "VEHICLE":
                        item = new Vehicle(id, sellerId, name, desc, price, path, itemStatus, createdAt);
                        break;
                    case "OTHER":
                        item = new Electronic(id, sellerId, name, desc, price, path, itemStatus, createdAt);
                        break;
                    default:
                        item = new Electronic(id, sellerId, name, desc, price, path, itemStatus, createdAt);
                }

                if (item != null) {
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==================== ADMIN: Cập nhật trạng thái sản phẩm ====================
    public boolean updateItemStatus(int itemId, String newStatus) {
        String sql = "UPDATE items SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== MAPPING =====
    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        String typeFromDB = rs.getString("item_type");
        Item item;

        switch (typeFromDB) {
            case "ELECTRONIC":
                item = new Electronic();
                break;
            case "VEHICLE":
                item = new Vehicle();
                break;
            case "ART":
                item = new Art();
                break;
            case "OTHER":
                item = new Electronic();
                break;
            default:
                item = new Electronic();
                break;
        }

        item.setId(rs.getInt("id"));
        item.setSellerId(rs.getInt("seller_id"));
        item.setName(rs.getString("product_name"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getDouble("starting_price"));
        item.setImagePath(rs.getString("image_path"));
        item.setStatus(rs.getString("status"));

        return item;
    }

    public int countPendingItems() {
        String sql = "SELECT COUNT(*) FROM items WHERE status = 'WAITING';";
        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(">>> [LỖI DB] Không thể đếm người dùng trực tuyến: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }
}