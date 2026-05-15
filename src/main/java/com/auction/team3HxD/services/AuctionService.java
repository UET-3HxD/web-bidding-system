package com.auction.team3HxD.services;

import com.auction.team3HxD.dao.AuctionDAO;
import com.auction.team3HxD.model.Auction;
import com.auction.team3HxD.model.Item;
import com.auction.team3HxD.model.User;
import com.auction.team3HxD.network.AuctionServer;

public class AuctionService { // triển khai singleton

    private Item currentItem;
    private double currentMaxBid;
    private String highestBidder;
    private boolean isAuctionActive;
    private AuctionDAO auctionDAO = new AuctionDAO();
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
    public String getAuctionDetailMessage(int auctionId, int userId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return null;

        Item item = auction.getItem();
        User seller = auction.getSeller();
        if (item == null || seller == null) return null;

        String timeLeft = auction.getTimeLeftFormatted();
        String desc = item.getDescription() != null ? item.getDescription().replace("#", " ").replace("|", " ") : "Không có mô tả";
        String image = item.getImagePath() != null ? item.getImagePath() : "";

        double bidIncrement = auction.getStartPrice() * 0.02;

        String highestBidderName = auctionDAO.getHighestBidderName(auctionId);
        if (highestBidderName == null) highestBidderName = "Chưa có";

        double userLastBid = auctionDAO.getUserLastBid(auctionId, userId);
        String userLastBidStr = (userLastBid > 0) ? String.format("%.0f", userLastBid) : "---";
        // ĐỊNH DẠNG CHUẨN (12 TRƯỜNG):
        // 0:id, 1:name, 2:category, 3:sellerName, 4:sellerId, 5:start, 6:current, 7:increment, 8:timeLeft, 9:desc, 10:image 11:currentwinner
        return String.format("%d#%s#%s#%s#%d#%.0f#%.0f#%.0f#%s#%s#%s#%s#%s",
                auction.getId(),
                item.getName(),
                item.getItemType(),
                seller.getUsername(),
                seller.getId(),        // Trường số 4 (quan trọng để đồng bộ)
                auction.getStartPrice(),
                auction.getCurrentPrice(),
                bidIncrement,
                timeLeft,              // Lúc này timeLeft nằm ở index 8
                desc,                  // index 9
                image,
                highestBidderName,
                userLastBidStr);                // index 10
    }
    //helper
    public void startNewAuction(Item item) {
        this.currentItem = item;
        this.currentMaxBid = item.getStartingPrice();
        this.isAuctionActive = true;
        AuctionServer.broadcast("AUCTION_START|" + item.getName() + "|" + currentMaxBid);
    }
}
