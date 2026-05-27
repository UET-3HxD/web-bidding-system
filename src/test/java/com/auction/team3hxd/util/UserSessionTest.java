package com.auction.team3hxd.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    private UserSession userSession;

    @BeforeEach
    void setUp() {
        userSession = UserSession.getInstance();
        // Đảm bảo trạng thái sạch trước mỗi lần test
        userSession.logout();
    }

    @Test
    @DisplayName("Kiểm tra logic lưu ID phòng đấu giá được chọn")
    void testSelectedAuctionId() {
        userSession.setSelectedAuctionId(99);
        assertEquals(99, userSession.getSelectedAuctionId());
    }

    @Test
    @DisplayName("Đảm bảo thông tin bị xóa hoàn toàn sau khi đăng xuất")
    void testLogoutClearsAllData() {
        // Giả lập trạng thái đã thiết lập ID phòng đấu giá
        userSession.setSelectedAuctionId(5);

        // Gọi hàm đăng xuất
        userSession.logout();

        // Xác thực dữ liệu đã quay về mặc định
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getUsername());
        assertNull(userSession.getRole());
        assertNull(userSession.getEmail());
        assertEquals(-1, userSession.getSelectedAuctionId());
        assertEquals(0, userSession.getId());
    }
}