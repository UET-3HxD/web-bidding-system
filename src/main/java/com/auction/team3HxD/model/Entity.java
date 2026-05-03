package com.auction.team3HxD.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Entity {
    protected int id;
    protected LocalDateTime createdAt;

    // Constructor
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