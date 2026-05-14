package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.*;
import com.auction.team3HxD.util.DBConnection;

import com.auction.team3HxD.model.Item;
import com.auction.team3HxD.util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;
import java.util.UUID;

public class ItemDAO {

    private final UserDAO userDAO = new UserDAO();

    public Item findById(int id) { // Đảm bảo là int chứ không phải UUID
        String sql = "SELECT * FROM items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); // Dùng setInt
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean saveItem(Item item, String type) {
        // 1. Thêm cột item_type vào câu lệnh SQL
        String sql = "INSERT INTO items (seller_id, product_name, starting_price, description, image_path, item_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 2. Lấy dữ liệu từ đối tượng Item (Polymorphism)
            pstmt.setInt(1, item.getSellerId());
            pstmt.setString(2, item.getName());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setString(4, item.getDescription());
            pstmt.setString(5, item.getImagePath());

            // 3. Sử dụng tham số type được truyền vào từ UserService
            // Đảm bảo type khớp với ENUM ('ELECTRONIC', 'ART', 'VEHICLE') trong MySQL
            pstmt.setString(6, type.toUpperCase());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lưu Item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getAllItemsBySeller(int sellerId) {
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ?";

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
                // Sử dụng getObject cho LocalDateTime
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
                }
                if (item != null) itemList.add(item);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return itemList;
    }
    public List<MyProductDTO> getMyProducts(int userId) {
        List<MyProductDTO> list = new ArrayList<>();

        // Nửa trên: Lấy đồ MÌNH BÁN (Chờ duyệt, Đã duyệt, Đang sàn, Đã đóng)
        // Nửa dưới (Sau UNION): Lấy đồ MÌNH THẮNG (Mệnh danh 'WON')
        String sql =
                "SELECT i.id, i.product_name, i.item_type, i.image_path, i.starting_price, " +
                        "   CASE " +
                        "       WHEN a.id IS NULL THEN i.status " + // Nếu chưa có phiên đấu giá -> WAITING hoặc APPROVED
                        "       WHEN a.end_time > NOW() THEN 'ACTIVE' " + // Phiên đang chạy
                        "       ELSE 'CLOSED' " + // Phiên đã kết thúc
                        "   END AS tag_status " +
                        "FROM items i " +
                        "LEFT JOIN auction_sessions a ON i.id = a.item_id " +
                        "WHERE i.seller_id = ? " +

                        "UNION " +

                        "SELECT i.id, i.product_name, i.item_type, i.image_path, a.current_price as starting_price, " +
                        "   'WON' AS tag_status " +
                        "FROM items i " +
                        "JOIN auction_sessions a ON i.id = a.item_id " +
                        "WHERE a.end_time <= NOW() AND " +
                        "   (SELECT user_id FROM bids WHERE auction_id = a.id ORDER BY bid_amount DESC LIMIT 1) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId); // Cho nửa đồ mình bán
            ps.setInt(2, userId); // Cho nửa đồ mình thắng

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Đóng gói dữ liệu vào DTO hoặc gửi thẳng dạng chuỗi qua Socket
                // Ví dụ: list.add(new MyProductDTO(rs.getInt("id"), ..., rs.getString("tag_status")));
            }
        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }
    public boolean updateItemInfo(int itemId, String name, double price, String desc) {
        // Chỉ update nếu sản phẩm KHÔNG ở trạng thái LIVE hoặc SOLD
        String sql = "UPDATE items SET product_name = ?, starting_price = ?, description = ?, status = 'WAITING' " +
                "WHERE id = ? AND status NOT IN ('LIVE', 'SOLD')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setString(3, desc);
            pstmt.setInt(4, itemId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteItem(int itemId) {
        // Chỉ cho phép xóa nếu sản phẩm KHÔNG ở trạng thái LIVE hoặc SOLD để đảm bảo tính minh bạch của sàn
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
            default:
                item = new Electronic();
                break;
        }

        // Sau khi đã có đối tượng (đã được khởi tạo lớp con), ta nạp các thuộc tính chung
        item.setId(rs.getInt("id"));
        item.setSellerId(rs.getInt("seller_id"));
        item.setName(rs.getString("product_name"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getDouble("starting_price"));
        item.setImagePath(rs.getString("image_path"));
        item.setStatus(rs.getString("status"));

        return item;
    }
}
