package com.auction.team3HxD.util;

/**
 * Lớp tiện ích Singleton lưu trữ thông tin phiên làm việc của người dùng.
 * Dùng để biết ai đang đăng nhập, vai trò, trạng thái online trên client.
 */
public class UserSession {
    private static UserSession instance;
    private String username;
    private String role;      // "BIDDER", "SELLER", "ADMIN"
    private boolean loggedIn;
    private int selectedAuctionId = -1;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Gọi khi đăng nhập thành công.
     * @param username tên đăng nhập
     * @param role vai trò (BIDDER/SELLER/ADMIN)
     */
    public void login(String username, String role) {
        this.username = username;
        this.role = role;
        this.loggedIn = true;
    }

    /**
     * Đăng xuất, xóa thông tin.
     */
    public void logout() {
        this.username = null;
        this.role = null;
        this.loggedIn = false;
    }

    public void setSelectedAuctionId(int id) { this.selectedAuctionId = id; }
    public int getSelectedAuctionId() { return selectedAuctionId; }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}