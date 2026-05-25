package com.auction.team3HxD.services;

import com.auction.team3HxD.dao.AuctionDAO;
import com.auction.team3HxD.dto.AuctionContextDTO;
import com.auction.team3HxD.model.Auction;
import com.auction.team3HxD.model.Item;
import com.auction.team3HxD.model.User;
import com.auction.team3HxD.network.AuctionServer;
import com.auction.team3HxD.util.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public class AuctionService { // triển khai singleton

    private Item currentItem;
    private double currentMaxBid;
    private String highestBidder;
    private boolean isAuctionActive;
    private AuctionDAO auctionDAO = new AuctionDAO();
    private final BidValidator bidValidator = new BidValidator();
    private final AntiSnipingService antiSnipingService = new AntiSnipingService();


    public String placeBid(int auctionId, int userId, double bidAmount) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            if (!auctionDAO.lockAuctionSession(conn, auctionId)) {
                conn.rollback();
                return "BID_ERROR|Không tìm thấy phiên đấu giá!";
            }

            AuctionContextDTO context = auctionDAO.getAuctionContext(conn, auctionId);
            if (context == null) {
                conn.rollback();
                return "BID_ERROR|Phòng đấu giá không tồn tại hoặc đã bị gỡ.";
            }

            String validationError = bidValidator.validate(context, bidAmount);
            if (validationError != null) {
                conn.rollback();
                return validationError;
            }

            auctionDAO.updateAuctionPrice(conn, auctionId, bidAmount);
            auctionDAO.insertBidHistory(conn, auctionId, userId, bidAmount);

            boolean isExtended = antiSnipingService.processAntiSnipe(conn, auctionDAO, auctionId, context.getTimeLeft());

            conn.commit();

            if (isExtended) {
                return "BID_SUCCESS_EXTENDED|" + bidAmount + "|" + context.getProductName();
            } else {
                return "BID_SUCCESS|" + bidAmount;
            }

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return "BID_ERROR|Lỗi hệ thống khi đặt giá!";
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
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
