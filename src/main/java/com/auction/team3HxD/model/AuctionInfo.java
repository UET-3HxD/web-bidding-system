package com.auction.team3HxD.model;

public class AuctionInfo {

  private final int id;
  private final String productName;
  private final double currentPrice;
  private final String endTime;
  private final String seller;

  public AuctionInfo(int id, String productName, double currentPrice, String endTime,
      String seller) {
    this.id = id;
    this.productName = productName;
    this.currentPrice = currentPrice;
    this.endTime = endTime;
    this.seller = seller;
  }

  public int getId() {
    return id;
  }

  public String getProductName() {
    return productName;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public String getEndTime() {
    return endTime;
  }

  public String getSeller() {
    return seller;
  }
}