package com.auction.team3hxd.services;

import com.auction.team3hxd.dao.UserDAO;
import com.auction.team3hxd.model.User;
import com.auction.team3hxd.model.NormalUser;
import com.auction.team3hxd.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;
    private UserDAO mockUserDAO;

    @BeforeEach
    void setUp() throws Exception {
        userService = new UserService();
        mockUserDAO = mock(UserDAO.class);

        Field fieldDAO = UserService.class.getDeclaredField("userDAO");
        fieldDAO.setAccessible(true);
        fieldDAO.set(userService, mockUserDAO);

        Field fieldOnline = UserService.class.getDeclaredField("ONLINE_USERS");
        fieldOnline.setAccessible(true);
        Set<String> ONLINE_USERS = (Set<String>) fieldOnline.get(null);
        ONLINE_USERS.clear();
    }

    @Test
    @DisplayName("Đăng ký thất bại nếu mật khẩu ngắn hơn 6 ký tự")
    void testRegisterWeakPasswordReturnsError() {
        String result = userService.register("user1", "123", "test@gmail.com");
        assertEquals("REG_ERR_WEAK_DATA", result);
    }

    @Test
    @DisplayName("Đăng ký thất bại nếu sai định dạng email")
    void testRegisterInvalidEmailReturnsError() {
        String result = userService.register("user1", "1234567", "invalid-email");
        assertEquals("REG_ERR_INVALID_EMAIL", result);
    }

    @Test
    @DisplayName("Đăng nhập thất bại khi tài khoản đang online")
    void testLoginAlreadyOnlineReturnsError() {
        User fakeUser = new NormalUser("hieu", "pass123", "hieu@gmail.com", Role.USER);
        when(mockUserDAO.getUserByUsername("hieu")).thenReturn(fakeUser);

        userService.login("hieu", "pass123");
        String result = userService.login("hieu", "pass123");

        assertEquals("LOGIN_ERR_ALREADY_ONLINE", result);
    }
}