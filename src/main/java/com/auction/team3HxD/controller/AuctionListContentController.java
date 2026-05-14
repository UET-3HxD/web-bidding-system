package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.NavigationHost;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.Optional;

public class AuctionListContentController implements NavigationConsumer {

    @FXML private TextField txtSearch;
    @FXML private FlowPane auctionContainer;
    @FXML private Button btnFilter;

    private NavigationHost navigationHost;

    public void setNavigationHost(NavigationHost host) {
        this.navigationHost = host;
    }

    // Dữ liệu mẫu
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

    private void handleJoinAuction(String itemName) {
        if (navigationHost != null) {
            navigationHost.navigateTo("/fxml/auction_detail_content.fxml", itemName);
        }
    }

    @FXML
    void handleSearch(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        auctionContainer.getChildren().clear();
        for (String[] item : sampleAuctions) {
            if (keyword.isEmpty() || item[0].toLowerCase().contains(keyword.toLowerCase())) {
                auctionContainer.getChildren().add(createAuctionCard(item));
            }
        }
    }

    @FXML
    void handleFilter(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bộ lọc");
        alert.setHeaderText("Chọn tiêu chí lọc");

        VBox content = new VBox(10);
        content.getChildren().add(new Label("Trạng thái:"));
        ChoiceBox<String> statusBox = new ChoiceBox<>();
        statusBox.getItems().addAll("Tất cả", "Đang đấu giá", "Sắp kết thúc");
        statusBox.setValue("Tất cả");
        content.getChildren().add(statusBox);

        content.getChildren().add(new Label("Giá tối thiểu:"));
        TextField minPrice = new TextField();
        content.getChildren().add(minPrice);

        content.getChildren().add(new Label("Giá tối đa:"));
        TextField maxPrice = new TextField();
        content.getChildren().add(maxPrice);

        alert.getDialogPane().setContent(content);
        ButtonType btnApply = new ButtonType("Áp dụng", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(btnApply, ButtonType.CANCEL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnApply) {
            filterAuctions(statusBox.getValue(), minPrice.getText(), maxPrice.getText());
        }
    }

    private void filterAuctions(String status, String minPriceStr, String maxPriceStr) {
        auctionContainer.getChildren().clear();
        for (String[] item : sampleAuctions) {
            String itemStatus = item[3];
            String priceStr = item[1].replace(" ₫", "").replace(",", "");
            double price = Double.parseDouble(priceStr);

            boolean statusOk = status.equals("Tất cả") ||
                    (status.equals("Đang đấu giá") && itemStatus.equals("Đang đấu giá")) ||
                    (status.equals("Sắp kết thúc") && itemStatus.equals("Sắp kết thúc"));

            boolean minOk = minPriceStr.isEmpty() || price >= Double.parseDouble(minPriceStr);
            boolean maxOk = maxPriceStr.isEmpty() || price <= Double.parseDouble(maxPriceStr);

            if (statusOk && minOk && maxOk) {
                auctionContainer.getChildren().add(createAuctionCard(item));
            }
        }
    }
}