package com.auction.team3hxd.services;

import com.auction.team3hxd.dao.AuctionDAO;
import com.auction.team3hxd.model.*;
import com.auction.team3hxd.dao.UserDAO;
import com.auction.team3hxd.model.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();
    private AuctionDAO auctionDAO = new AuctionDAO();

    public synchronized String register(String username, String password, String email) {
        if (username.isBlank() || password.length() < 6 || email.isBlank()) {
            return "REG_ERR_WEAK_DATA";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "REG_ERR_INVALID_EMAIL";
        }
        if (userDAO.getUserByUsername(username) != null) {
            return "REGISTER_ERR_USERNAME_EXISTS";
        }
        if (userDAO.getUserByEmail(email) != null) {
            return "REGISTER_ERR_EMAIL_EXISTS";
        }

        User newUser = new NormalUser(username, password, email, Role.USER);
        if (userDAO.insertUser(newUser) > 0) {
            return "REGISTER_OK";
        }
        return "REG_ERR_DATABASE";
    }

    public synchronized String login(String username, String password) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return "LOGIN_ERR_USER_NOT_FOUND";
        }
        if (user.getRole() == Role.BANNED) {
            return "LOGIN_ERR_BANNED";
        }
        if (!user.getPassword().equals(password)) {
            return "LOGIN_ERR_INVALID";

        }
        if (onlineUsers.contains(username)) {
            return "LOGIN_ERR_ALREADY_ONLINE";
        }

        onlineUsers.add(username);
        // trả về thành công kèm role
        return "LOGIN_OK|" + user.getRole().name() + "|" + user.getEmail();
    }
    public String changePassword(String username, String oldPass, String newPass) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) return "CP_ERR|USER_NOT_FOUND";

        if (!user.getPassword().equals(oldPass)) {
            return "CP_ERR|WRONG_PASSWORD";
        }

        boolean success = userDAO.updatePassword(username, newPass);
        return success ? "CP_SUCCESS" : "CP_ERR|DB_ERROR";
    }

    public String changeEmail(String username, String newEmail, String password) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) return "CE_ERR|USER_NOT_FOUND";

        if (!user.getPassword().equals(password)) {
            return "CE_ERR|WRONG_PASSWORD";
        }

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

    public int startAuction(int itemId, int durationMinutes) {
        return auctionDAO.startAuction(itemId, durationMinutes);
    }

    public String getLiveAuctionsMessage() {
        try {
            // 1. Lấy danh sách Object Auction
            List<Auction> auctions = auctionDAO.findLiveAuctions();
            System.out.println(">>> getLiveAuctionsMessage: found " + (auctions != null ? auctions.size() : 0) + " auctions");

            if (auctions == null || auctions.isEmpty()) {
                return "LIVE_AUCTIONS_EMPTY";
            }

            StringBuilder sb = new StringBuilder("LIVE_AUCTIONS_SUCCESS");

            // 2. Chuyển Object thành chuỗi
            for (Auction auction : auctions) {
                Item item = auction.getItem();
                User seller = auction.getSeller();

                System.out.println(">>> Auction ID=" + auction.getId() +
                        ", item=" + (item != null ? item.getName() : "NULL") +
                        ", seller=" + (seller != null ? seller.getUsername() : "NULL") +
                        ", status=" + auction.getStatus() +
                        ", endTime=" + auction.getEndTime());

                if (item == null || seller == null) {
                    System.err.println(">>> Cảnh báo: Bỏ qua Auction ID " + auction.getId());
                    continue;
                }

                String category = (item.getItemType() != null && item.getItemType().equals("VEHICLE")) ? "Phương tiện" :
                        (item.getItemType() != null && item.getItemType().equals("ART")) ? "Nghệ thuật" : "Điện tử";

                String timeLeftStr = auction.getTimeLeftFormatted();
                String desc = item.getDescription() != null ? item.getDescription().replace("|", "-").replace("#", "-") : "";
                double currentDisplayPrice = auction.getCurrentPrice() > 0 ? auction.getCurrentPrice() : auction.getStartPrice();

                String formattedRow = String.format("%d#%s#%s#%s#%d#%.0f#%.0f#%d#%s#%s#%s",
                        auction.getId(),
                        item.getName(),
                        category,
                        seller.getUsername(),
                        seller.getId(),
                        auction.getStartPrice(),
                        currentDisplayPrice,
                        auction.getBidCount(),
                        timeLeftStr,
                        desc,
                        item.getImagePath() != null ? item.getImagePath() : "");

                sb.append("|").append(formattedRow);
            }

            System.out.println(">>> getLiveAuctionsMessage result: " + sb.toString());
            return sb.toString();

        } catch (Exception e) {
            System.err.println(">>> ERROR in getLiveAuctionsMessage: " + e.getMessage());
            e.printStackTrace();
            return "LIVE_AUCTIONS_EMPTY";
        }
    }

    // ==================== ADMIN ====================

    /**
     * Kiểm tra người dùng có phải Admin không.
     */
    public boolean isAdmin(int userId) {
        User user = userDAO.getUserById(userId);
        return user != null && user.getRole() == Role.ADMIN;
    }


    // ==================== USER MANAGEMENT (ADMIN) ====================

    /**
     * Lấy danh sách tất cả người dùng (id, username, email, role).
     * Trả về chuỗi ALL_USERS|id#username#email#role|...
     */
    public String getAllUsersList() {
        List<User> users = userDAO.findAll();
        if (users == null || users.isEmpty()) {
            return "ALL_USERS_EMPTY";
        }
        StringBuilder sb = new StringBuilder("ALL_USERS");
        for (User u : users) {
            sb.append("|").append(u.getId())
                    .append("#").append(u.getUsername())
                    .append("#").append(u.getEmail() != null ? u.getEmail() : "")
                    .append("#").append(u.getRole().name());
        }
        return sb.toString();
    }

    public boolean unbanUser(int userId) {
        return userDAO.updateRole(userId, "USER");
    }

}
