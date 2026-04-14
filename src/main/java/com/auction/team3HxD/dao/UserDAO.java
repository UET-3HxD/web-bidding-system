package com.auction.team3HxD.dao;

import com.auction.team3HxD.model.User;
import com.auction.team3HxD.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // CREATE -> thêm user vào database
    public void insert(User user) {
        String sql = "INSERT INTO users(name, email) VALUES (?, ?)"; // ? = phần giữ chỗ

        try (Connection conn = DBConnection.getConnection(); // kết nối database
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // gán giá trị vào "?"
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());

            ps.executeUpdate();
            // thực thi INSERT, UPDATE, DELETE
            System.out.println("Insert thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ ALL -> lấy toàn bộ user
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DBConnection.getConnection(); // kết nối database
             PreparedStatement ps = conn.prepareStatement(sql);
             // nhận dữ liệu từ database
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) { // duyệt từng dòng
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
                list.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // READ BY ID -> tìm user theo Id
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }

    // UPDATE
    public void update(User user) {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getId());

            ps.executeUpdate();
            System.out.println("Update thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Delete thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // GET USER BY NAME (phục vụ Đăng nhập/ Đăng ký)
    public User getUserByUsername(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        User user = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return user; // null nếu không tìm thấy
    }
}
