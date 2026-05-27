package com.auction.team3hxd.model;

import java.time.LocalDateTime;

/**
 * Lớp trừu tượng dại diện cho 1 thực thể trong hệ thống.
 *
 * @author Huy
 **/
public abstract class Entity {

    protected int id;
    protected LocalDateTime createdAt;

    /**
     * Phương thức khởi tạo.
     */
    public Entity() {
        this.id = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
