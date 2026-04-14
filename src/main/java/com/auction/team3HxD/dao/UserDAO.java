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

    // CREATE -> thêm một user mới vaò database
    public void insert(User user) {
        String sql = "INSERT INTO users(id, user_name, password_hash, email, role) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getUserName());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());

            ps.executeUpdate();
            System.out.println("Insert thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public User findById(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    // UPDATE
    public void update(User user) {
        String sql = "UPDATE users SET user_name = ?, password_hash = ?, email = ?, role = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getId().toString());

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
        String sql = "SELECT * FROM users WHERE user_name = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    // ================= HELPER =================
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new NormalUser(
                UUID.fromString(rs.getString("id")),
                rs.getString("user_name"),
                rs.getString("password_hash"),
                rs.getString("email"),
                Role.valueOf(rs.getString("role"))
        );
    }
}