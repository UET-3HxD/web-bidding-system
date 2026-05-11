package com.auction.team3HxD.services;

import com.auction.team3HxD.model.NormalUser;
import com.auction.team3HxD.model.User;
import com.auction.team3HxD.dao.UserDAO;
import com.auction.team3HxD.model.enums.Role;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public synchronized String register(String username, String password, String email) {
        if (username.isBlank() || password.length() < 6 || email.isBlank()) {
            return "REG_ERR_WEAK_DATA";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "REG_ERR_INVALID_EMAIL";
        }
        if (userDAO.getUserByUsername(username) != null) {
            return "REGISTER_ERR_USERNAME_EXISTS";   // đúng như client mong đợi
        }
        if (userDAO.getUserByEmail(email) != null) {
            return "REGISTER_ERR_EMAIL_EXISTS";      // đúng như client mong đợi
        }

        User newUser = new NormalUser(username, password, email, Role.USER);
        if (userDAO.insertUser(newUser) > 0) {
            return "REGISTER_OK";                    // thành công
        }
        return "REG_ERR_DATABASE";
    }

    public synchronized String login(String username, String password) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return "LOGIN_ERR_USER_NOT_FOUND";
        }

        if (!user.getPassword().equals(password)) {
            return "LOGIN_ERR_INVALID"; // Sai user hoặc pass

        }
        if (onlineUsers.contains(username)) {
            return "LOGIN_ERR_ALREADY_ONLINE";       // đúng như client mong đợi
        }

        onlineUsers.add(username);
        // trả về thành công kèm role
        return "LOGIN_OK|" + user.getRole().name() + "|" + user.getEmail();
    }
    public String changePassword(String username, String oldPass, String newPass) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) return "CP_ERR|USER_NOT_FOUND";

        // Kiểm tra mật khẩu cũ có khớp không
        if (!user.getPassword().equals(oldPass)) {
            return "CP_ERR|WRONG_PASSWORD";
        }

        // Cập nhật mật khẩu mới
        boolean success = userDAO.updatePassword(username, newPass);
        return success ? "CP_SUCCESS" : "CP_ERR|DB_ERROR";
    }

    public String changeEmail(String username, String newEmail, String password) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) return "CE_ERR|USER_NOT_FOUND";

        // Phải đúng mật khẩu mới cho đổi Email
        if (!user.getPassword().equals(password)) {
            return "CE_ERR|WRONG_PASSWORD";
        }

        // Kiểm tra email mới đã có ai dùng chưa
        if (userDAO.getUserByEmail(newEmail) != null) {
            return "CE_ERR|EMAIL_TAKEN";
        }

        boolean success = userDAO.updateEmail(username, newEmail);
        return success ? "CE_SUCCESS" : "CE_ERR|DB_ERROR";
    }
    public void logout(String username) {
        if (username != null) {
            onlineUsers.remove(username);
        }
    }
}