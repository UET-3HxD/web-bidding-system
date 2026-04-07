package com.auction.team3HxD.model;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Entity {
    private UUID id;
    private LocalDateTime createdAt;

    // Constructor
    public Entity() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}