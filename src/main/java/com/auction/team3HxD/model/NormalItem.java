package com.auction.team3HxD.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class NormalItem extends Item {

  public NormalItem(int sellerId, String name, String desc, double price, String path) {
    super(sellerId, name, desc, price, path);
  }

  public NormalItem(int id, int sellerId, String name, String desc, double price,
      String path, String status, LocalDateTime createdAt) {
    super(id, sellerId, name, desc, price, path, status, createdAt);
  }

  public String getItemType() {
    return "NORMAL";
  }
}
