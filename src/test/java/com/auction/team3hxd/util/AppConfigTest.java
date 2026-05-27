package com.auction.team3hxd.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @Test
    @DisplayName("Kiểm tra các giá trị cấu hình mặc định của hệ thống")
    void testDefaultConfigValues() {
        // Kiểm tra xem Host cấu hình có tồn tại và không bị trống
        assertNotNull(AppConfig.getServerHost());
        assertFalse(AppConfig.getServerHost().trim().isEmpty());

        // Kiểm tra Port của server phải nằm trong dải cổng hợp lệ (0 - 65535)
        int port = AppConfig.getServerPort();
        assertTrue(port > 0 && port <= 65535);
    }
}