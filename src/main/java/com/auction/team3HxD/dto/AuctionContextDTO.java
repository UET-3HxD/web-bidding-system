package com.auction.team3HxD.dto;

public class AuctionContextDTO {
  private String status;
  private int timeLeft;
  private String productName;
  private double startPrice;
  private double currentHighestPrice;

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public int getTimeLeft() { return timeLeft; }
  public void setTimeLeft(int timeLeft) { this.timeLeft = timeLeft; }

  public String getProductName() { return productName; }
  public void setProductName(String productName) { this.productName = productName; }

  public double getStartPrice() { return startPrice; }
  public void setStartPrice(double startPrice) { this.startPrice = startPrice; }

  public double getCurrentHighestPrice() { return currentHighestPrice; }
  public void setCurrentHighestPrice(double currentHighestPrice) { this.currentHighestPrice = currentHighestPrice; }
}