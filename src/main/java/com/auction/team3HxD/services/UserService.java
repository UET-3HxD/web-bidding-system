package com.auction.team3HxD.services;

import com.auction.team3HxD.dao.AuctionDAO;
import com.auction.team3HxD.dao.ItemDAO;
import com.auction.team3HxD.model.*;
import com.auction.team3HxD.dao.UserDAO;
import com.auction.team3HxD.model.enums.Role;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private final ItemDAO itemDAO = new ItemDAO();
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
    public boolean updateItem(int itemId, String name, double price, String desc) {
        // 1. (Tùy chọn) Kiểm tra trạng thái hiện tại trong DB một lần nữa để đảm bảo an toàn
        // 2. Gọi DAO để cập nhật và reset trạng thái về WAITING
        return itemDAO.updateItemInfo(itemId, name, price, desc);
    }
    // Xử lý tạo sản phẩm dựa trên loại (Polymorphism)
    public boolean createItem(int sellerId, String name, double price, String type, String desc, String path) {
        Item newItem;
        switch (type.toUpperCase()) {
            case "ELECTRONIC":
                newItem = new Electronic(sellerId, name, desc, price, path);
                break;
            case "ART":
                newItem = new Art(sellerId, name, desc, price, path);
                break;
            case "VEHICLE":
                newItem = new Vehicle(sellerId, name, desc, price, path);
                break;
            default:
                return false;
        }
        return itemDAO.saveItem(newItem, type);
    }
    // Lấy danh sách sản phẩm và định dạng thành chuỗi phản hồi cho Socket
    public String getMyItemsList(int sellerId) {
        List<Item> items = itemDAO.getAllItemsBySeller(sellerId);
        if (items.isEmpty()) return "LIST_ITEMS_EMPTY";

        StringBuilder sb = new StringBuilder("LIST_ITEMS_SUCCESS");
        for (Item item : items) {
            sb.append("|").append(item.getId())
                    .append("#").append(item.getName())
                    .append("#").append(item.getPrice())
                    .append("#").append(item.getStatus())
                    .append("#").append(item.getImagePath())
                    .append("#").append(item.getDescription());
        }
        return sb.toString();
    }
    public boolean startAuction(int itemId, int durationMinutes) {
        return auctionDAO.startAuction(itemId, durationMinutes);
    }
    public boolean deleteItem(int itemId) {
        return itemDAO.deleteItem(itemId);
    }
    public String getLiveAuctionsMessage() {
        // 1. Lấy danh sách Object Auction (Sử dụng hàm findLiveAuctions() mà DAO trả về List<Auction>)
        List<Auction> auctions = auctionDAO.findLiveAuctions();

        if (auctions == null || auctions.isEmpty()) {
            return "LIVE_AUCTIONS_EMPTY";
        }

        StringBuilder sb = new StringBuilder("LIVE_AUCTIONS_SUCCESS");

        // 2. Chuyển Object thành chuỗi bằng cách gọi các Method bên trong Object
        for (Auction auction : auctions) {
            Item item = auction.getItem();
            User seller = auction.getSeller();

            // CHỐT CHẶN AN TOÀN: Nếu không thấy Item, bỏ qua phiên này để không gây lỗi Null
            if (item == null || seller == null) {
                System.err.println(">>> Cảnh báo: Bỏ qua Auction ID " + auction.getId() + " do không tìm thấy Item hoặc Seller tương ứng.");
                continue;
            }

            // Logic xử lý category và thời gian (giữ nguyên như cũ)
            String category = (item.getItemType() != null && item.getItemType().equals("VEHICLE")) ? "Phương tiện" :
                    (item.getItemType() != null && item.getItemType().equals("ART")) ? "Nghệ thuật" : "Điện tử";

            String timeLeftStr = auction.getTimeLeftFormatted();
            String desc = item.getDescription() != null ? item.getDescription().replace("|", "-").replace("#", "-") : "";
            double currentDisplayPrice = auction.getCurrentPrice() > 0 ? auction.getCurrentPrice() : auction.getStartPrice();

            // Nối chuỗi gửi về Client
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

        return sb.toString();
    }
}
