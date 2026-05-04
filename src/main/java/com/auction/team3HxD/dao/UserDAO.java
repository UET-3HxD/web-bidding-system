package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.NormalUser;
import com.auction.team3HxD.model.User;
import com.auction.team3HxD.model.enums.Role;
import com.auction.team3HxD.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ================= CREATE =================
    public int insertUser(User user) {
        String sql = "INSERT INTO users(username, password, email, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setId(id);
                    return id;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Insert user failed", e);
        }

        return -1;
    }

    // ================= READ ALL =================
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find all users failed", e);
        }

        return list;
    }

    // ================= READ BY ID =================
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id); // ✅ sửa đúng kiểu

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find user by id failed", e);
        }

        return null;
    }

    // ================= UPDATE =================
    public void update(User user) {
        String sql = "UPDATE users SET username=?, password=?, email=?, role=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.getId()); // ✅ FIX QUAN TRỌNG

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Update user failed", e);
        }
    }

    // ================= DELETE =================
    public void delete(int id) { // ✅ UUID -> int
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id); // ✅ sửa
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Delete user failed", e);
        }
    }

    // ================= LOGIN =================
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find user by username failed", e);
        }

        return null;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Find user by email failed", e);
        }

        return null;
    }

    // ================= HELPER =================
    private User mapResultSetToUser(ResultSet rs) throws SQLException {

        NormalUser user = new NormalUser(
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                Role.valueOf(rs.getString("role").toUpperCase())
        );

        user.setId(rs.getInt("id"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }

        return user;
    }
}