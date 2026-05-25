package com.auction.team3HxD.model;

import java.time.LocalDateTime;

public class BidTransaction extends Entity {

  private int id;
  private User bidder;
  private double amount;
  private LocalDateTime timestamp;

  // Getter and setter
  public User getBidder() {
    return bidder;
  }

  public double getAmount() {
    return amount;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  // Constructor
  public BidTransaction(User bidder, double amount) {
    this.bidder = bidder;
    this.amount = amount;
    this.timestamp = LocalDateTime.now();
  }
}
