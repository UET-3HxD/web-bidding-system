package com.auction.team3HxD.model.observer;

import java.time.LocalDateTime;

/**
 * Lớp chứa thông tin mỗi khi có người đặt giá mới
 */
public class BidEvent {
  private String auctionId;
  private double newPrice;
  private String newLeader;
  private LocalDateTime timestamp;

  // Constructor
  public BidEvent(String auctionId , double newPrice , String newLeader) {
    this.auctionId = auctionId;
    this.newPrice = newPrice;
    this.newLeader = newLeader;
    this.timestamp = LocalDateTime.now();
  }

  //Getter setter
  public String getAuctionId() {return auctionId;}

  public double getNewPrice() {return newPrice;}

  public String getNewLeader() {return newLeader;}
}
