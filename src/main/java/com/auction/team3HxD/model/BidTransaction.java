package com.auction.team3HxD.model;

import java.time.LocalDateTime;

public class BidTransaction extends Entity{
    private int id;
    private Bidder bidder;
    private double amount;
    private LocalDateTime timestamp;

    // Getter and setter
    public Bidder getBidder() {return bidder;}

    public double getAmount() {return amount;}

    public LocalDateTime getTimestamp() {return timestamp;}

    // Constructor
    public BidTransaction(Bidder bidder , double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
}
