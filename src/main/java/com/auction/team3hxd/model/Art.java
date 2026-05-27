package com.auction.team3hxd.model;

import java.time.LocalDateTime;

/**
 * Lớp đại diện cho sản phẩm thuộc danh mục Art.
 *
 * @author Huy
 *
 */
public class Art extends Item {

    /**
     * Khởi tạo không tham số.
     */
    public Art() {
        super();
    }

    /**
     * Phương thức khởi tạo dùng chung.
     *
     * @param sellerId id người bán
     * @param name     tên sản phẩm
     * @param desc     mô tả
     * @param price    giá khởi điểm
     * @param path     đường dẫn ảnh
     */
    public Art(int sellerId, String name, String desc, double price, String path) {
        super(sellerId, name, desc, price, path);
    }

    /**
     * Phương thức khởi tạo đầy đủ.
     *
     * @param id        id sản phẩm
     * @param status    trạng thái
     * @param createdAt thời điểm tạo
     */
    public Art(
            int id,
            int sellerId,
            String name,
            String desc,
            double price,
            String path,
            String status,
            LocalDateTime createdAt) {
        super(id, sellerId, name, desc, price, path, status, createdAt);
    }

    public String getItemType() {
        return "ART";
    }
}
