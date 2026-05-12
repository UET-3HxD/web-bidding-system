package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

public class AuctionListController {

    @FXML private TextField txtSearch;
    @FXML private FlowPane auctionContainer;
    @FXML private Label lblAvatarShort;
    @FXML private Label lblSidebarName;

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
        lblAvatarShort.setText("ND");
        lblSidebarName.setText("Nguyễn Minh Đức");
        loadSampleAuctions();
    }

    private void loadSampleAuctions() {
        auctionContainer.getChildren().clear();
        for (String[] item : sampleAuctions) {
            auctionContainer.getChildren().add(createAuctionCard(item));
        }
    }

    private VBox createAuctionCard(String[] data) {
        String title = data[0];
        String price = data[1];
        String time = data[2];
        String statusText = data[3];
        String statusClass = data[4];
        String emoji = data[5];

        VBox card = new VBox(12);
        card.getStyleClass().add("auction-card");

        StackPane imagePane = new StackPane();
        imagePane.getStyleClass().add("auction-image");
        Label imgLabel = new Label(emoji);
        imgLabel.getStyleClass().add("auction-image-placeholder");
        imagePane.getChildren().add(imgLabel);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("auction-title");

        Label lblPrice = new Label(price);
        lblPrice.getStyleClass().add("auction-price");

        Label lblTime = new Label(time);
        lblTime.getStyleClass().add("auction-time");

        Label lblStatus = new Label(statusText);
        lblStatus.getStyleClass().addAll("status-badge", statusClass);

        Button btnBid = new Button("Đấu giá ngay");
        btnBid.getStyleClass().add("bid-now-btn");
        btnBid.setOnAction(e -> handleJoinAuction(title));

        card.getChildren().addAll(imagePane, lblTitle, lblPrice, lblTime, lblStatus, btnBid);
        return card;
    }

    // ================= ĐIỀU HƯỚNG MENU =================
    @FXML
    void handleMenuAccount(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/account_view.fxml",
                (Node) event.getSource(), "Tài khoản");
    }

    @FXML
    void handleMenuMyBids(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/my_bids.fxml",
                (Node) event.getSource(), "Bid đang tham gia");
    }

    @FXML
    void handleMenuMyProducts(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/seller_products.fxml",
                (Node) event.getSource(), "Sản phẩm của bạn");
    }

    @FXML
    void handleMenuHelp(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/help.fxml",
                (Node) event.getSource(), "Trợ giúp");
    }

    @FXML
    void handleSearch(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadSampleAuctions();
        } else {
            auctionContainer.getChildren().clear();
            for (String[] item : sampleAuctions) {
                if (item[0].toLowerCase().contains(keyword.toLowerCase())) {
                    auctionContainer.getChildren().add(createAuctionCard(item));
                }
            }
        }
    }

    @FXML
    void handleGoHome() {
        // Đã ở Sàn đấu giá, có thể refresh hoặc không làm gì
    }

    private void handleJoinAuction(String itemName) {
        // Mở chi tiết đấu giá (có thể dùng SceneSwitcher sau khi có file)
        System.out.println("Tham gia đấu giá: " + itemName);
    }
}