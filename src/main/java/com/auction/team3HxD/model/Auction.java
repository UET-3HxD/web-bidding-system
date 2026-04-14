package com.auction.team3HxD.model;

import com.auction.team3HxD.model.enums.AuctionStatus;
import com.auction.team3HxD.model.observer.AuctionObserver;
import com.auction.team3HxD.exception.AuctionClosedException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Auction extends Entity{
    //private int id;
    private Item item;
    private Seller seller;
    private AuctionStatus status;
    private LocalDateTime startTime , endTime;
    private double startPrice;
    private double currentPrice;
    private Bidder currentWinner;
    private List<BidTransaction> bidHistory;
    private List<AuctionObserver> observers;

    //Constructor
    public Auction(Seller seller , Item item , double startPrice , String startTime , String endTime) {
        this.seller = seller;
        this.item = item;
        this.startPrice = startPrice;
        this.startTime = LocalDateTime.parse(startTime);
        this.endTime = LocalDateTime.parse(endTime);
        this.bidHistory = new ArrayList<>();
        this.observers = new ArrayList<>();
        status = AuctionStatus.OPEN;
    }

    //Start : status -> RUNNING / Cho phép bid
    public void start() {
        status = AuctionStatus.RUNNING;
    }

    // Đặt bid , kiêm tra hợp lệ
    // Synchronized : Không nhiều
    public synchronized boolean placeBid(Bidder bidder , double amount) throws Exception{
        //Kiểm tra xem auction đã cho phép bid chưa
        if (status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Auction has not RUNNING yet");
        }


        return true;
    }
}
