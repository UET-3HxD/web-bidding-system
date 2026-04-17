package com.auction.team3HxD.services;

import com.auction.team3HxD.model.NormalUser;
import com.auction.team3HxD.model.User;
import com.auction.team3HxD.dao.UserDAO;
import com.auction.team3HxD.model.enums.Role;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    // Danh sách để quản lý những người đang online (Tránh login 2 nơi)
    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    // logic đăng ký: kiểm tra tính hợp lệ
    public synchronized String register(String username, String password, String email) {
        // 1. Kiểm tra các trường bị trống
        if (username.isBlank() || password.length() < 6 || email.isBlank()) {
            return "REG_ERR_WEAK_DATA";
        }

        // 2. Kiểm tra định dạng Email (Sử dụng Util của bạn B nếu có)
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "REG_ERR_INVALID_EMAIL";
        }

        // 3. Kiểm tra trùng Username
        if (userDAO.getUserByUsername(username) != null) {
            return "REG_ERR_USER_EXISTS";
        }

        // 4. Kiểm tra trùng Email (Yêu cầu bạn DAO làm hàm này)
        if (userDAO.getUserByEmail(email) != null) {
            return "REG_ERR_EMAIL_EXISTS";
        }

        // 5. Nếu mọi thứ ổn, tạo NormalUser với Role cố định
        User newUser = new NormalUser(username, password, email, Role.USER);

        if (userDAO.insertUser(newUser)) {
            return "REG_SUCCESS";
        }

        return "REG_ERR_DATABASE";
    }

    // LOGIC ĐĂNG NHẬP
    public String login(String username, String password) {
        User user = userDAO.getUserByUsername(username);

        if (user == null || !user.getPasswordHash().equals(password)) {
            return "LOGIN_ERR_INVALID"; // Sai user hoặc pass
        }

        // XỬ LÝ TRANH CHẤP: Kiểm tra xem có ai đang dùng tài khoản này không
        if (onlineUsers.contains(username)) {
            return "LOGIN_ERR_ALREADY";
        }

        onlineUsers.add(username);
        return "LOGIN_SUCCESS";
    }

    public void logout(String username) {
        if (username != null) onlineUsers.remove(username);
    }
}