package com.auction.team3hxd.services;

import com.auction.team3hxd.dao.ItemDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    private ItemService itemService;
    private ItemDAO mockItemDAO;

    @BeforeEach
    void setUp() throws Exception {
        itemService = new ItemService();
        mockItemDAO = mock(ItemDAO.class);

        Field field = ItemService.class.getDeclaredField("itemDAO");
        field.setAccessible(true);
        field.set(itemService, mockItemDAO);
    }

    @Test
    @DisplayName("Chặn dữ liệu chứa ký tự cấm hoặc bị trống trong validator")
    void testItemValidatorInvalidDataReturnsFalse() {
        assertFalse(itemService.itemValidator("Name#", "100", "Desc", "ELECTRONIC"));
        assertFalse(itemService.itemValidator("Name", "100", "Desc|", "ELECTRONIC"));
        assertFalse(itemService.itemValidator("", "100", "Desc", "ELECTRONIC"));
        assertFalse(itemService.itemValidator("Laptop", "abc", "Desc", "ELECTRONIC"));
    }

    @Test
    @DisplayName("Chấp nhận định dạng dữ liệu sạch")
    void testItemValidatorValidDataReturnsTrue() {
        assertTrue(itemService.itemValidator("Laptop Dell", "15000000", "May dep", "ELECTRONIC"));
    }

    @Test
    @DisplayName("Trả về -1 khi tạo sản phẩm với danh mục không hợp lệ")
    void testCreateItemInvalidCategoryReturnsMinusOne() {
        int result = itemService.createItem(1, "Ghe", 200, "FURNITURE", "Mo ta", "path");
        assertEquals(-1, result);
    }
}