package com.auction.team3HxD.network;

import com.auction.team3HxD.model.observer.NotificationManager;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionTimeoutTask {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startChecking() {
        // Cứ mỗi 1 giây, lao vào Database kiểm tra một lần
        scheduler.scheduleAtFixedRate(this::checkAndCloseAuctions, 0, 1, TimeUnit.SECONDS);
        System.out.println(">>> [SERVER] Hệ thống giám sát thời gian đấu giá đã kích hoạt (1s/lần).");
    }

    private void checkAndCloseAuctions() {
        // Lấy các phiên đã quá giờ (end_time <= NOW()) nhưng vẫn đang ở trạng thái 'ACTIVE'
        String selectSql = "SELECT id FROM auction_sessions WHERE end_time <= NOW() AND status = 'ACTIVE'";

        try (Connection conn = com.auction.team3HxD.util.DBConnection.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(selectSql);
             ResultSet rs = psSelect.executeQuery()) {

            while (rs.next()) {
                int auctionId = rs.getInt("id");
                processAuctionEnd(auctionId, conn);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void processAuctionEnd(int auctionId, Connection conn) throws SQLException {
        // 1. Tìm xem ai là người đặt giá cao nhất trong bảng bids cho phiên này
        String winnerSql = "SELECT user_id, bid_amount FROM bids WHERE auction_id = ? ORDER BY bid_amount DESC LIMIT 1";
        int winnerId = -1; // Mặc định -1 tức là không có ai bid, sản phẩm bị ế
        double finalPrice = 0;

        try (PreparedStatement psWinner = conn.prepareStatement(winnerSql)) {
            psWinner.setInt(1, auctionId);
            try (ResultSet rsWinner = psWinner.executeQuery()) {
                if (rsWinner.next()) {
                    winnerId = rsWinner.getInt("user_id");
                    finalPrice = rsWinner.getDouble("bid_amount");
                }
            }
        }

        // 2. Cập nhật trạng thái phiên đấu giá thành 'FINISHED' để lần sau không quét lại nữa
        String updateSql = "UPDATE auction_sessions SET status = 'FINISHED' WHERE id = ?";
        try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
            psUpdate.setInt(1, auctionId);
            psUpdate.executeUpdate();
        }

        // 3. 🌟 SỬ DỤNG OBSERVER ĐỂ PHÁT SÓNG TOÀN HỆ THỐNG
        // Gói tin: AUCTION_ENDED | ID phiên | ID người thắng
        String payload = auctionId + "|" + winnerId;
        NotificationManager.getInstance().notifyAllObservers("AUCTION_ENDED", payload);

        System.out.println(">>> [SERVER TRỌNG TÀI] Phiên " + auctionId + " đã KẾT THÚC. Người thắng: User " + winnerId);
    }
}
