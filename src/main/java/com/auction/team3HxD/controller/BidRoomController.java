package com.auction.team3HxD.controller;

import com.auction.team3HxD.dao.AuctionDAO;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.io.File;
import java.text.DecimalFormat;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import com.auction.team3HxD.util.SceneSwitcher;
import javafx.scene.Node;

public class BidRoomController {

    // --- FXML BINDING ---
    @FXML private Label lblBreadcrumbName, lblProductName, lblSeller, lblCategory, lblDescription;
    @FXML private Label lblStatusBadge, lblTimeLeft, lblMinIncrement, lblHighestBid, lblYourLastBid;
    @FXML private ImageView imgProductLarge;
    @FXML private VBox vboxNoImage;
    @FXML private TextField txtBidInput;
    @FXML private Button btnPlaceBid;

    private Timeline countdownTimeline;
    private int remainingSeconds = 0;
    private String currentAuctionId;
    private double currentHighestPrice = 0;
    private double minIncrement = 0;
    private DecimalFormat df = new DecimalFormat("#,###");
    private AuctionDAO auctionDAO = new AuctionDAO();
    private double myLastBid = 0;

    @FXML
    public void initialize() {
        // 1. Lấy ID phiên đấu giá từ "Bộ nhớ tạm" (Giả sử Captain lưu trong UserSession)
        // currentAuctionId = com.auction.team3HxD.util.UserSession.getInstance().getSelectedAuctionId();

        // Demo ID nếu chưa có logic truyền (Captain nhớ thay bằng logic thật)
        this.currentAuctionId = String.valueOf(com.auction.team3HxD.util.UserSession.getInstance().getSelectedAuctionId());
        int myId = com.auction.team3HxD.util.UserSession.getInstance().getId();
        // 2. Đăng ký nhận tin nhắn Real-time từ Server
        com.auction.team3HxD.util.SocketService.getInstance().setMessageHandler(this::handleServerResponse);

        // 3. Gửi yêu cầu lấy chi tiết phiên đấu giá
        System.out.println(">>> Đang lấy dữ liệu chi tiết cho Auction ID: " + currentAuctionId);
        com.auction.team3HxD.util.SocketService.getInstance().send("GET_AUCTION_DETAIL|" + currentAuctionId + "|" + myId);
    }

    private void handleServerResponse(String message) {
        Platform.runLater(() -> {
            String[] parts = message.split("\\|");
            String cmd = parts[0];

            switch (cmd) {
                case "AUCTION_DETAIL_SUCCESS":
                    // Chỉ bốc parts[1] khi chắc chắn mảng có đủ phần tử
                    if (parts.length > 1) {
                        displayAuctionInfo(parts[1]);
                    }
                    break;

                case "BID_SUCCESS":
                    showAlert("Thành công", "Bạn đã đặt giá thành công!", Alert.AlertType.INFORMATION);

                    // Cập nhật dòng chữ màu đỏ bằng số tiền vừa nhập
                    if (txtBidInput.getText() != null && !txtBidInput.getText().isEmpty()) {
                        myLastBid = Double.parseDouble(txtBidInput.getText());
                        currentHighestPrice = myLastBid;
                        lblYourLastBid.setText(df.format(Double.parseDouble(txtBidInput.getText())) + " VNĐ");
                        updateYourLastBidDisplay();
                    }
                    txtBidInput.clear();
                    break;

                case "BID_ERROR":
                    if (parts.length > 1) {
                        showAlert("Lỗi đặt giá", parts[1], Alert.AlertType.ERROR);
                    }
                    break;

                case "BID_UPDATE":
                    if (parts.length > 1) {
                        updateNewBid(parts[1]);
                    }
                    break;
            }
        });
    }

    private void displayAuctionInfo(String data) {
        String[] info = data.split("#");
        if (info.length < 11) return; // Kiểm tra an toàn

        // Cập nhật text
        lblBreadcrumbName.setText(info[1]);
        lblProductName.setText(info[1]);
        lblCategory.setText(info[2]);
        lblSeller.setText(info[3]);

        // Các chỉ số giá
        currentHighestPrice = Double.parseDouble(info[6]);
        minIncrement = Double.parseDouble(info[7]);
        lblMinIncrement.setText(df.format(minIncrement) + " VNĐ");
        lblHighestBid.setText(df.format(currentHighestPrice) + " VNĐ");

        // Xử lý THỜI GIAN (Lấy từ index 8)
        String timeLeftStr = info[8];
        parseAndStartCountdown(timeLeftStr);

        // Xử lý MÔ TẢ (Lấy từ index 9)
        lblDescription.setText(info[9]);

        // Xử lý ẢNH (Lấy từ index 10)
        String imagePath = info[10];
        if (!imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                imgProductLarge.setImage(new Image(file.toURI().toString()));
                vboxNoImage.setVisible(false);
                imgProductLarge.setVisible(true);
            } else {
                System.err.println(">>> Không tìm thấy file ảnh tại: " + imagePath);
                vboxNoImage.setVisible(true);
            }
        }
        String highestBidderName = info[11];
        double lastBid = auctionDAO.getUserLastBid(Integer.parseInt(currentAuctionId), com.auction.team3HxD.util.UserSession.getInstance().getId());
        if (lastBid == 0) {
            lblYourLastBid.setText("---");
        } else {
            lblYourLastBid.setText(df.format(lastBid) + " VNĐ");
        }
        updateYourLastBidDisplay();
    }
    private void parseAndStartCountdown(String timeLeftStr) {
        if (timeLeftStr == null || timeLeftStr.equals("Đã kết thúc")) {
            remainingSeconds = 0;
            lblTimeLeft.setText("Đã kết thúc");
            lblTimeLeft.setStyle("-fx-text-fill: #F43F5E;"); // Đỏ hồng cảnh báo
            btnPlaceBid.setDisable(true);
            txtBidInput.setDisable(true);
            return;
        }

        // Tách chuỗi "HH:mm:ss" thành mảng số nguyên
        String[] timeParts = timeLeftStr.split(":");
        try {
            if (timeParts.length == 3) {
                remainingSeconds = Integer.parseInt(timeParts[0]) * 3600 +
                        Integer.parseInt(timeParts[1]) * 60 +
                        Integer.parseInt(timeParts[2]);
            } else if (timeParts.length == 2) {
                remainingSeconds = Integer.parseInt(timeParts[0]) * 60 +
                        Integer.parseInt(timeParts[1]);
            }

            // Gọi hàm bắt đầu đếm ngược (hàm startCountdown() mà bạn đã thêm ở bước trước)
            startCountdown();

        } catch (NumberFormatException e) {
            lblTimeLeft.setText(timeLeftStr); // Fallback nếu dữ liệu lỗi
        }
    }
    private void checkInitialWinningStatus() {
        // Nếu có logic so sánh currentWinnerName từ Server trả về với Username của mình
        // thì bạn sẽ để if/else ở đây. Hiện tại khi mới mở phòng, ta đặt mặc định là "Sẵn sàng".

        // Reset Badge về trạng thái trung lập
        lblStatusBadge.setText("Sẵn sàng");
        lblStatusBadge.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white;"); // Màu xanh dương (Primary)

        // (Tùy chọn) Gán giá trị rỗng cho nhãn Your Last Bid nếu chưa từng đặt
        double lastBid = auctionDAO.getUserLastBid(Integer.parseInt(currentAuctionId), com.auction.team3HxD.util.UserSession.getInstance().getId());
        if (lastBid == 0) {
            lblYourLastBid.setText("---");
        } else {
            lblYourLastBid.setText(df.format(lastBid) + " VNĐ");
        }
    }
    private void updateNewBid(String data) {
        // Data: auctionId#newPrice#bidderName
        String[] info = data.split("#");
        double newPrice = Double.parseDouble(info[1]);
        String bidderName = info[2];

        currentHighestPrice = newPrice;
        Platform.runLater(() -> {
            lblHighestBid.setText(df.format(currentHighestPrice) + " VNĐ");
            // Cập nhật lại Badge dựa trên người vừa bid
            updateYourLastBidDisplay();
        });
    }

    @FXML
    void handlePlaceBid(ActionEvent event) {
        String input = txtBidInput.getText().trim();
        if (input.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số tiền!", Alert.AlertType.WARNING);
            return;
        }

        try {
            double bidAmount = Double.parseDouble(input);

            // Validate sơ bộ tại Client (Tránh gửi rác lên Server)
            if (bidAmount < currentHighestPrice + minIncrement) {
                showAlert("Giá quá thấp", "Bạn phải đặt tối thiểu: " + df.format(currentHighestPrice + minIncrement) + " VNĐ", Alert.AlertType.WARNING);
                return;
            }

            // Gửi lệnh đặt giá lên Server: PLACE_BID|auctionId|userId|amount
            // String userId = com.auction.team3HxD.util.UserSession.getInstance().getUserId();
            String userId = "1"; // Demo
            com.auction.team3HxD.util.SocketService.getInstance().send("PLACE_BID|" + currentAuctionId + "|" + userId + "|" + bidAmount);

        } catch (NumberFormatException e) {
            showAlert("Lỗi định dạng", "Vui lòng chỉ nhập số!", Alert.AlertType.ERROR);
        }
    }

    // Hàm sự kiện khi bấm nút "✖ Đóng phòng"
    @FXML void handleExitRoom(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/main_auction.fxml", (Node) event.getSource(), "Sàn Đấu Giá");
    }

    // Hàm dùng chung để cập nhật màu sắc thông minh
    private void updateYourLastBidDisplay() {
        myLastBid = auctionDAO.getUserLastBid(Integer.parseInt(currentAuctionId), com.auction.team3HxD.util.UserSession.getInstance().getId());
        if (myLastBid <= 0) {
            lblYourLastBid.setText("---");
            lblYourLastBid.setStyle("-fx-text-fill: #94A3B8;"); // Màu xám nhạt nếu chưa bid
            return;
        }

        lblYourLastBid.setText(df.format(myLastBid) + " VNĐ");

        // So sánh giá của mình với giá cao nhất hiện tại
        if (myLastBid >= currentHighestPrice) {
            // Đang dẫn đầu -> Đổi sang màu XANH LÁ (giống giá hiện tại)
            lblYourLastBid.setStyle("-fx-text-fill: #10B981; -fx-font-size: 22px; -fx-font-weight: bold;");
        } else {
            // Bị outbid -> Đổi sang màu ĐỎ HỒNG
            lblYourLastBid.setStyle("-fx-text-fill: #F43F5E; -fx-font-size: 22px; -fx-font-weight: bold;");
        }
    }
    private void startCountdown() {
        // 1. Dọn dẹp Timeline cũ nếu có (để tránh lỗi đếm lùi nhanh gấp đôi nếu gọi hàm 2 lần)
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        // 2. Tạo một KeyFrame chạy lặp lại mỗi 1 giây
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (remainingSeconds > 0) {
                remainingSeconds--; // Trừ đi 1 giây

                // Tính toán lại Giờ, Phút, Giây
                int hours = remainingSeconds / 3600;
                int minutes = (remainingSeconds % 3600) / 60;
                int seconds = remainingSeconds % 60;

                // Format lại chuỗi hiển thị thành dạng HH:mm:ss
                lblTimeLeft.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            } else {
                // Khi hết giờ
                lblTimeLeft.setText("Đã kết thúc");
                lblTimeLeft.setStyle("-fx-text-fill: #F43F5E;"); // Đổi màu đỏ hồng cảnh báo

                btnPlaceBid.setDisable(true); // Khóa luôn nút đặt giá
                txtBidInput.setDisable(true); // Khóa ô nhập tiền

                countdownTimeline.stop(); // Dừng đồng hồ
            }
        }));

        countdownTimeline.setCycleCount(Timeline.INDEFINITE); // Chạy vô hạn cho đến khi bị stop()
        countdownTimeline.play(); // Bắt đầu đếm!
    }

    @FXML void handleGoToAccount(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
    }
    @FXML void handleGoToProducts(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/product_management.fxml", (Node) event.getSource(), "Quản lý sản phẩm");
    }
    // Thêm các nút thiếu
    @FXML void handleGoToMyBids(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/my_bids.fxml", (Node) event.getSource(), "Bid đang tham gia");
    }
    @FXML void handleGoToHelp(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/help.fxml", (Node) event.getSource(), "Trợ giúp");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
