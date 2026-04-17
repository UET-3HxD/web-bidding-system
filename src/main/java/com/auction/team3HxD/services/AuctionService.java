package com.auction.team3HxD.services;

import com.auction.team3HxD.model.Item;
import com.auction.team3HxD.network.AuctionServer;

public class AuctionService { // triển khai singleton

    private Item currentItem;
    private double currentMaxBid;
    private String highestBidder;
    private boolean isAuctionActive;

    // LOGIC XỬ LÝ TRANH CHẤP GIÁ (Cực kỳ quan trọng)
    // Dùng synchronized để đảm bảo tại 1 thời điểm chỉ 1 người được trả giá
    public synchronized String placeBid(String username, double amount) {
        if (!isAuctionActive) {
            return "BID_ERR_CLOSED";
        }

        if (amount <= currentMaxBid) {
            return "BID_ERR_LOW";
        }

        // nếu không lỗi & bid hợp lệ
        this.currentMaxBid = amount;
        this.highestBidder = username;

        // thông báo nếu có bid cao nhất mới
        AuctionServer.broadcast("BID_UPDATE|" + username + "|" + amount);

        return "BID_SUCCESS";
    }

    //helper
    public void startNewAuction(Item item) {
        this.currentItem = item;
        this.currentMaxBid = item.getStartingPrice();
        this.isAuctionActive = true;
        AuctionServer.broadcast("AUCTION_START|" + item.getName() + "|" + currentMaxBid);
    }
}
