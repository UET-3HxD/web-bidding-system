package com.auction.team3hxd.services;

import com.auction.team3hxd.dao.AuctionDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AntiSnipingServiceTest {

    private AntiSnipingService antiSnipingService;
    private Connection mockConnection;
    private AuctionDAO mockAuctionDAO;

    @BeforeEach
    void setUp() {
        antiSnipingService = new AntiSnipingService();
        mockConnection = mock(Connection.class);
        mockAuctionDAO = mock(AuctionDAO.class);
    }

    @Test
    @DisplayName("Gia hạn thành công khi thời gian còn lại từ 1 đến 30 giây")
    void testProcessAntiSnipeWithin30SecondsReturnsTrue() throws SQLException {
        boolean result = antiSnipingService.processAntiSnipe(mockConnection, mockAuctionDAO, 1, 15);
        assertTrue(result);
        verify(mockAuctionDAO, times(1)).extendEndTime(mockConnection, 1);
    }

    @Test
    @DisplayName("Không gia hạn nếu thời gian còn lại lớn hơn 30 giây")
    void testProcessAntiSnipeMoreThan30SecondsReturnsFalse() throws SQLException {
        boolean result = antiSnipingService.processAntiSnipe(mockConnection, mockAuctionDAO, 1, 45);
        assertFalse(result);
        verify(mockAuctionDAO, never()).extendEndTime(any(), anyInt());
    }

    @Test
    @DisplayName("Không gia hạn nếu thời gian nhỏ hơn hoặc bằng 0")
    void testProcessAntiSnipeExpiredTimeReturnsFalse() throws SQLException {
        boolean result = antiSnipingService.processAntiSnipe(mockConnection, mockAuctionDAO, 1, 0);
        assertFalse(result);
        verify(mockAuctionDAO, never()).extendEndTime(any(), anyInt());
    }
}