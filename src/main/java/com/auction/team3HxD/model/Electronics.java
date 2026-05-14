package com.auction.team3HxD.model;

import java.time.LocalDateTime;

public class Electronic extends Item {
    public Electronic(int sellerId, String name, String desc, double price, String path) {
        super(sellerId, name, desc, price, path);
    }
    public Electronic(int id, int sellerId, String name, String desc, double price,
                      String path, String status, LocalDateTime createdAt) {
        super(id, sellerId, name, desc, price, path, status, createdAt);
    }
    public String getItemType(){
        return "ELECTRONIC";
    }
    public Electronic(){
        super();
    }
}

