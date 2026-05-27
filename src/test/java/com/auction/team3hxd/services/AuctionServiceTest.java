package com.auction.team3hxd.services;

import com.auction.team3hxd.dao.AuctionDAO;
import com.auction.team3hxd.dto.AuctionContextDTO;
import com.auction.team3hxd.util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.lang.reflect.Field;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionServiceTest {

    private AuctionService auctionService;
    private AuctionDAO mockAuctionDAO;
    private Connection mockConnection;

    @BeforeEach
    void setUp() throws Exception {
        auctionService = new AuctionService();
        mockAuctionDAO = mock(AuctionDAO.class);
        mockConnection = mock(Connection.class);

        Field field = AuctionService.class.getDeclaredField("auctionDAO");
        field.setAccessible(true);
        field.set(auctionService, mockAuctionDAO);
    }

    @Test
    @DisplayName("Trả về lỗi khi không khóa được phiên đấu giá")
    void testPlaceBidLockFailedReturnsError() throws Exception {
        try (MockedStatic<DBConnection> mockedDb = mockStatic(DBConnection.class)) {
            mockedDb.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockAuctionDAO.lockAuctionSession(any(), anyInt())).thenReturn(false);

            String result = auctionService.placeBid(1, 10, 500000);
            assertTrue(result.contains("BID_ERROR|Không tìm thấy phiên đấu giá!"));
        }
    }

    @Test
    @DisplayName("Trả về lỗi khi thông tin phòng đấu giá trống")
    void testPlaceBidContextNullReturnsError() throws Exception {
        try (MockedStatic<DBConnection> mockedDb = mockStatic(DBConnection.class)) {
            mockedDb.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockAuctionDAO.lockAuctionSession(any(), anyInt())).thenReturn(true);
            when(mockAuctionDAO.getAuctionContext(any(), anyInt())).thenReturn(null);

            String result = auctionService.placeBid(1, 10, 500000);
            assertTrue(result.contains("Phòng đấu giá không tồn tại"));
        }
    }
}