package com.auction.team3HxD.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.event.ActionEvent;

public class AuctionListController {

    @FXML
    private TextField txtSearch;

    @FXML
    private FlowPane auctionContainer;

    @FXML
    private Label lblAvatarShort;

    @FXML
    private Label lblSidebarName;

    // Dữ liệu mẫu tạm thời (sẽ thay bằng service)
    private final String[][] sampleAuctions = {
            {"iPhone 15 Pro Max", "12.500.000 ₫", "⏳ Còn 2 giờ 15 phút", "Đang đấu giá", "status-badge-active", "🖼"},
            {"MacBook Pro M3", "34.200.000 ₫", "⏳ Còn 4 giờ 20 phút", "Đang đấu giá", "status-badge-active", "💻"},
            {"Đồng hồ Rolex Submariner", "280.000.000 ₫", "⏳ Còn 45 phút", "Sắp kết thúc", "status-badge-ending", "⌚"},
            {"Tai nghe Sony WH-1000XM5", "4.200.000 ₫", "⏳ Còn 1 ngày", "Đang đấu giá", "status-badge-active", "🎧"},
            {"Nike Air Jordan 1 High", "12.000.000 ₫", "⏳ Còn 3 giờ", "Đang đấu giá", "status-badge-active", "👟"},
            {"Máy ảnh Canon EOS R6", "49.000.000 ₫", "⏳ Còn 1 giờ 10 phút", "Sắp kết thúc", "status-badge-ending", "📷"},
            {"PlayStation 5", "10.500.000 ₫", "⏳ Còn 5 giờ", "Đang đấu giá", "status-badge-active", "🎮"},
            {"Laptop Dell XPS 15", "24.800.000 ₫", "⏳ Còn 2 ngày", "Đang đấu giá", "status-badge-active", "💻"}
    };

    @FXML
    public void initialize() {
        // Gán dữ liệu mẫu cho sidebar (sau này sẽ lấy từ UserService)
        lblAvatarShort.setText("ND");   // Ví dụ: Nguyễn Văn A -> "NV"
        lblSidebarName.setText("Nguyễn Minh Đức");

        // Hiển thị danh sách đấu giá mẫu
        loadSampleAuctions();
    }

    private void loadSampleAuctions() {
        auctionContainer.getChildren().clear();
        for (String[] item : sampleAuctions) {
            auctionContainer.getChildren().add(createAuctionCard(item));
        }
    }

    /**
     * Tạo một card đấu giá với style class đã định nghĩa trong CSS.
     */
    private VBox createAuctionCard(String[] data) {
        String title = data[0];
        String price = data[1];
        String time = data[2];
        String statusText = data[3];
        String statusClass = data[4];
        String emoji = data[5];

        VBox card = new VBox(12);
        card.getStyleClass().add("auction-card");

        // Ảnh placeholder
        StackPane imagePane = new StackPane();
        imagePane.getStyleClass().add("auction-image");
        Label imgLabel = new Label(emoji);
        imgLabel.getStyleClass().add("auction-image-placeholder");
        imagePane.getChildren().add(imgLabel);

        // Tên sản phẩm
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("auction-title");

        // Giá hiện tại
        Label lblPrice = new Label(price);
        lblPrice.getStyleClass().add("auction-price");

        // Thời gian còn lại
        Label lblTime = new Label(time);
        lblTime.getStyleClass().add("auction-time");

        // Trạng thái
        Label lblStatus = new Label(statusText);
        lblStatus.getStyleClass().addAll("status-badge", statusClass);

        // Nút "Đấu giá ngay"
        Button btnBid = new Button("Đấu giá ngay");
        btnBid.getStyleClass().add("bid-now-btn");
        btnBid.setOnAction(e -> handleJoinAuction(title));

        card.getChildren().addAll(imagePane, lblTitle, lblPrice, lblTime, lblStatus, btnBid);
        return card;
    }

    // ==================== CÁC HÀNH ĐỘNG MENU ====================
    @FXML
    private void handleMenuAccount(ActionEvent event) {
        // Chuyển sang màn Tài khoản (account_view.fxml)
        System.out.println("Chuyển hướng: Tài khoản");
        // TODO: Load account_view.fxml vào cùng stage
    }

    @FXML
    private void handleMenuMyBids(ActionEvent event) {
        // Chuyển sang màn Bid đang tham gia
        System.out.println("Chuyển hướng: Bid đang tham gia");
        // TODO: Load my_bids.fxml
    }

    @FXML
    private void handleMenuMyProducts(ActionEvent event) {
        // Chuyển sang màn Sản phẩm của bạn
        System.out.println("Chuyển hướng: Sản phẩm của bạn");
        // TODO: Load seller_products.fxml
    }

    @FXML
    private void handleMenuHelp(ActionEvent event) {
        // Chuyển sang màn Trợ giúp
        System.out.println("Chuyển hướng: Trợ giúp");
        // TODO: Load help.fxml
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadSampleAuctions(); // hiện lại tất cả
        } else {
            // Lọc tạm đơn giản
            auctionContainer.getChildren().clear();
            for (String[] item : sampleAuctions) {
                if (item[0].toLowerCase().contains(keyword.toLowerCase())) {
                    auctionContainer.getChildren().add(createAuctionCard(item));
                }
            }
        }
    }

    @FXML
    private void handleGoHome() {
        // Giả sử quay về auction list (hiện tại đã ở đây)
        System.out.println("Về trang chủ (Sàn đấu giá)");
        // Có thể reload hoặc giữ nguyên
    }

    private void handleJoinAuction(String itemName) {
        // Mở màn hình chi tiết đấu giá (auction_detail.fxml)
        System.out.println("Tham gia đấu giá: " + itemName);
        // TODO: Load auction_detail.fxml và truyền ID sản phẩm
    }
}