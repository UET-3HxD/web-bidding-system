package com.auction.team3hxd.model;

import java.time.LocalDateTime;

/**
 * Lớp đại diện cho thông tin lần giao dịch.
 *
 * @author Huy
 * */
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

  /**
   * Phương thức khởi tạo.
   *
   * @param bidder người đấu giá
   * @param amount số tiền đấu giá
   */
  public BidTransaction(User bidder, double amount) {
    this.bidder = bidder;
    this.amount = amount;
    this.timestamp = LocalDateTime.now();
  }
}
