package com.auction.team3hxd.model;

import java.time.LocalDateTime;

/**
 * Lớp đại diện cho đối tượng là sản phẩm thuộc danh mục Electronics.
 *
 * @author Huy
 *
 */
public class Electronic extends Item {
    /**
     * Khởi tạo không tham số.
     */
    public Electronic() {
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
    public Electronic(int sellerId, String name, String desc, double price, String path) {
        super(sellerId, name, desc, price, path);
    }

    /**
     * Phương thức khởi tại đầy đủ thông tin.
     *
     * @param id        id sản phẩm
     * @param status    trạng thái
     * @param createdAt thời điểm tạo
     */
    public Electronic(int id, int sellerId, String name, String desc, double price,
                      String path, String status, LocalDateTime createdAt) {
        super(id, sellerId, name, desc, price, path, status, createdAt);
    }

    public String getItemType() {
        return "ELECTRONIC";
    }
}

