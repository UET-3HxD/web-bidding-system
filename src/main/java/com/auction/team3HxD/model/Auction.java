package com.auction.team3HxD.model;

import com.auction.team3HxD.exception.InvalidBidException;
import com.auction.team3HxD.model.enums.AuctionStatus;
import com.auction.team3HxD.model.observer.AuctionObserver;
import com.auction.team3HxD.exception.AuctionClosedException;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Duration;

public class Auction extends Entity{
    private Item item;
    private User seller;

    private AuctionStatus status;
    private LocalDateTime startTime , endTime;

    private double startPrice;
    private double currentPrice;
    private double bidIncrement;
    private int bidCount;
    private User currentWinner;
    private List<BidTransaction> bidHistory;

    private final Lock lock = new ReentrantLock();

    //Getter and Setter
    public double getCurrentPrice() {return currentPrice;}

    public User currentWinner() {return currentWinner;}

    //Constructor 1
    public Auction(User seller , Item item , double startPrice , double bidIncrement , String startTime , String endTime) {
        this.seller = seller;
        this.item = item;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.startTime = LocalDateTime.parse(startTime);
        this.endTime = LocalDateTime.parse(endTime);
        this.bidHistory = new ArrayList<>();
        status = AuctionStatus.OPEN;
    }

    // Constructor 2
    public Auction(int id, User seller, Item item,
                   double startPrice, double currentPrice, double bidIncrement,
                   AuctionStatus status,
                   LocalDateTime startTime, LocalDateTime endTime) {

        this.setId(id);
        this.seller = seller;
        this.item = item;

        this.startPrice = startPrice;
        this.currentPrice = currentPrice;
        this.bidIncrement = bidIncrement;

        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;

        this.bidHistory = new ArrayList<>();
    }

    //Start : status -> RUNNING / Cho phép bid
    public void start() {
        status = AuctionStatus.RUNNING;
    }

    // Đặt bid , kiểm tra hợp lệ
    // Synchronized : Không nhiều luồng đặt bid cùng lúc
    public void placeBid(User bidder , double amount) throws AuctionClosedException , InvalidBidException{
        lock.lock();
        try {
            //Kiểm tra xem auction đã cho phép bid chưa
            if (status != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Auction has not RUNNING yet or has FINISHED");
            }

            // Kiểm tra bid hợp lệ
            if (amount < currentPrice + bidIncrement) {
                throw new InvalidBidException("Invalid Bid");
            }

            // Cập nhật giá và người thắng
            currentPrice = amount;
            currentWinner = bidder;

            // Thêm vào lịch sử giao dịch
            bidHistory.add(new BidTransaction(bidder , amount));
        }
        finally {
            lock.unlock();
        }
    }

    // Kết thúc đấu giá
    public void endAuction() {
        status = AuctionStatus.FINISHED;
    }

    // Kéo dài thời gian (Anti Sniping)
    public void extendTime(int time) throws AuctionClosedException{
        if (status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Auction has not RUNNING yet or has FINISHED");
        }

        LocalDateTime newTime = endTime.plusHours(1);
        endTime = newTime;
    }
    public String getTimeLeftFormatted() {
        if (status == AuctionStatus.FINISHED || LocalDateTime.now().isAfter(endTime)) {
            return "Đã kết thúc";
        }

        Duration duration = Duration.between(LocalDateTime.now(), endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
//    GETTER
    public User getSeller() { return seller; }
    public Item getItem() { return item; }
    public double getStartPrice() { return startPrice; }
    public double getBidIncrement() { return bidIncrement; }
    public AuctionStatus getStatus() { return status; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getBidCount() {
        return bidCount;
    }
    public void setBidCount(int bidCount) {
        this.bidCount = bidCount;
    }
}
