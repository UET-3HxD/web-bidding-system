package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.NormalUser;
import com.auction.team3HxD.model.User;
import com.auction.team3HxD.model.enums.Role;
import com.auction.team3HxD.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {

    // CREATE -> thêm một user mới vào database
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users(username, password, email, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // READ ALL -> show all -> trả về toàn bộ user trong DB
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = mapResultSetToUser(rs); // chuyển biến dữ liệu SQL sang Java Object
                list.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // READ BY ID -> tìm kiếm bằng Id
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(id));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    // UPDATE
    public void update(User user) {
        String sql = "UPDATE users SET username=?, password=?, email=?, role=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());

            ps.executeUpdate();
            System.out.println("Update thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void delete(UUID id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();
            System.out.println("Delete thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // LOGIN / REGISTER SUPPORT
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();

             PreparedStatement ps = conn.prepareStatement(sql)) {

            // THỬ NGHIỆM: Quét toàn bộ bảng để xem Java thấy bao nhiêu người
            try (Statement stmt = conn.createStatement();
                 ResultSet rsAll = stmt.executeQuery("SELECT username, LENGTH(username) FROM users")) {

                System.out.println("--- DANH SÁCH USER JAVA THẤY ---");
                int count = 0;
                while (rsAll.next()) {
                    count++;
                    System.out.println("User: [" + rsAll.getString(1) + "] - Độ dài: " + rsAll.getInt(2));
                }
                System.out.println("Tổng cộng Java thấy: " + count + " users.");
                System.out.println("--------------------------------");
            }

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    // ================= HELPER =================
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        NormalUser user = new NormalUser(
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                Role.valueOf(rs.getString("role").toUpperCase())
        );

        // set id
        user.setId(rs.getInt("id"));

        // set createdAt
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }

        return user;
    }
}