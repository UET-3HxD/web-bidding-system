package com.auction.team3HxD.model;

import com.auction.team3HxD.exception.AuctionClosedException;
import com.auction.team3HxD.exception.InsufficientBalanceException;
import com.auction.team3HxD.exception.InvalidBidException;
import com.auction.team3HxD.model.enums.Role;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User{
    private double balance;
    private List<BidTransaction> bids;
    // Map autoBidProfiles

    // Constructor
    public Bidder(String userName , String passwordHash , String email , Role role , double balance) {
        super(userName , passwordHash , email , role);
        this.balance = balance;
        this.bids = new ArrayList<>();
    }

    // Getter and setter


    // Đặt bid, gọi placeBid của Auction
    public void placeBid(Auction auction , double amount) throws InsufficientBalanceException , AuctionClosedException , InvalidBidException {
        // Số bid lớn hơn tiền hiện có
        if (amount > balance) {
            throw new InsufficientBalanceException("Insufficient Balance");
        }

        auction.placeBid(this , amount);
    }

    // Đặt AutoBid (Item , double , int)

    // Lấy dữ liệu AutoBid ()
}
