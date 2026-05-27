package com.auction.team3hxd.model;

import java.time.LocalDateTime;

/**
 * Lớp trừu tượng đại diện cho một sản phẩm trong hệ thống.
 *
 * @author Huy
 *
 */
public abstract class Item extends Entity {

    private int sellerId;
    private String productName;
    private String description;
    private double startingPrice;
    private String imagePath;
    private String status;

    /**
     * Constructor chung cho các lớp con.
     */
    public Item() {
    }

    /**
     * Hàm khởi tạo đối tượng.
     *
     * @param sellerId      id người bán
     * @param productName   tên sản phẩm
     * @param description   mô tả sản phẩm
     * @param startingPrice giá khởi điểm
     * @param imagePath     đường dẫn ảnh
     */
    public Item(
            int sellerId,
            String productName,
            String description,
            double startingPrice,
            String imagePath) {
        this.sellerId = sellerId;
        this.productName = productName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.imagePath = imagePath;
    }

    /**
     * Constructor đầy đủ để nhận dữ liệu từ DB.
     *
     * @param id        id sản phẩm
     * @param status    trang thái sản phẩm (WAIT/APPROVED/SOLD)
     * @param createdAt thời gian tạo
     */
    public Item(
            int id,
            int sellerId,
            String productName,
            String description,
            double startingPrice,
            String imagePath,
            String status,
            LocalDateTime createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.productName = productName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.imagePath = imagePath;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * Phương thức lấy loại sản phẩm.
     *
     * @return loại sản phẩm
     */
    public abstract String getItemType();

    // Getter and setter
    public int getId() {
        return id;
    }

    public int getSellerId() {
        return sellerId;
    }

    public String getName() {
        return productName;
    }

    public double getPrice() {
        return startingPrice;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setName(String name) {
        this.productName = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setPrice(double price) {
        this.startingPrice = price;
    }
}
