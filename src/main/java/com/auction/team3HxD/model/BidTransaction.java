package com.auction.team3HxD.model;

import java.time.LocalDateTime;

public class BidTransaction extends Entity {

    private int id;
    private Auction auction;
    private Bidder bidder;
    private double amount;
    private LocalDateTime timestamp;

    // ===== CONSTRUCTOR =====
    public BidTransaction(Auction auction, Bidder bidder, double amount) {
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    // ===== GETTER =====
    public int getId() { return id; }

    public Auction getAuction() { return auction; }

    public Bidder getBidder() { return bidder; }

    public double getAmount() { return amount; }

    public LocalDateTime getTimestamp() { return timestamp; }

    // ===== SETTER =====
    public void setId(int id) { this.id = id; }

    public void setAuction(Auction auction) { this.auction = auction; }

    public void setBidder(Bidder bidder) { this.bidder = bidder; }

    public void setAmount(double amount) { this.amount = amount; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}