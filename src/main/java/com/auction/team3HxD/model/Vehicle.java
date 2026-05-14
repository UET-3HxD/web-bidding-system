package com.auction.team3HxD.model;

import java.time.LocalDateTime;

public class Vehicle extends Item{
    public Vehicle(int sellerId, String name, String desc, double price, String path) {
        super(sellerId, name, desc, price, path);
    }
    public Vehicle(int id, int sellerId, String name, String desc, double price,
                      String path, String status, LocalDateTime createdAt) {
        super(id, sellerId, name, desc, price, path, status, createdAt);
    }
    public String getItemType(){
        return "VEHICLE";
    }
    public Vehicle(){
        super();
    }
}
