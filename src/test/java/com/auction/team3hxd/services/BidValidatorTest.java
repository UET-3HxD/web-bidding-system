package com.auction.team3hxd.services;

import com.auction.team3hxd.dto.AuctionContextDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BidValidatorTest {

    private BidValidator bidValidator;
    private AuctionContextDTO ctx;

    @BeforeEach
    void setUp() {
        bidValidator = new BidValidator();
        ctx = new AuctionContextDTO();
        ctx.setStatus("ACTIVE");
        ctx.setTimeLeft(100);
        ctx.setStartPrice(100000); // 2% của 100k là 2k
    }

    @Test
    @DisplayName("Thất bại khi trạng thái không phải ACTIVE")
    void testValidateAuctionNotActiveReturnsError() {
        ctx.setStatus("CLOSED");
        String result = bidValidator.validate(ctx, 120000);
        assertNotNull(result);
        assertTrue(result.contains("Phiên đấu giá đã kết thúc"));
    }

    @Test
    @DisplayName("Lượt ra giá đầu tiên hợp lệ khi lớn hơn hoặc bằng giá khởi điểm")
    void testValidateFirstBidValidReturnsNull() {
        ctx.setCurrentHighestPrice(0);
        String result = bidValidator.validate(ctx, 100000);
        assertNull(result);
    }

    @Test
    @DisplayName("Lượt ra giá đầu tiên thất bại nếu thấp hơn giá khởi điểm")
    void testValidateFirstBidTooLowReturnsError() {
        ctx.setCurrentHighestPrice(0);
        String result = bidValidator.validate(ctx, 90000);
        assertNotNull(result);
        assertTrue(result.contains("phải lớn hơn hoặc bằng giá khởi điểm"));
    }

    @Test
    @DisplayName("Lượt ra giá tiếp theo phải cao hơn giá hiện tại và đủ bước giá")
    void testValidateNextBidNotEnoughIncrementReturnsError() {
        ctx.setCurrentHighestPrice(100000);
        String result = bidValidator.validate(ctx, 101000); // Chỉ tăng 1k (yêu cầu ít nhất 2k)
        assertNotNull(result);
        assertTrue(result.contains("bước giá tối thiểu"));
    }
}