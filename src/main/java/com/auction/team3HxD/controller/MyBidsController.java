package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;

public class MyBidsController {

    @FXML private Label lblSidebarName;
    @FXML private Label lblSidebarAvatar;
    @FXML private VBox vboxBidList;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private ImageView imgDetailImage;
    @FXML private Label lblDetailName;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailCategory;
    @FXML private Label lblDetailStartPrice;
    @FXML private Label lblDetailCurrentPrice;
    @FXML private Label lblDetailYourBid;
    @FXML private Button btnEnterRoom;

    private String selectedAuctionId = null;
    private String selectedStatus = null;

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username != null) {
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(2, username.length())).toUpperCase();
            lblSidebarAvatar.setText(shortName);
        }

        cbStatusFilter.getItems().addAll("Tất cả", "Đang diễn ra", "Đã thắng", "Đã thua");
        cbStatusFilter.getSelectionModel().selectFirst();
        cbStatusFilter.setOnAction(e -> filterBidList());

        SocketService.getInstance().setMessageHandler(this::handleServerResponse);
        SocketService.getInstance().send("GET_BID_HISTORY");
    }

    private void handleServerResponse(String response) {
        Platform.runLater(() -> {
            if (response.startsWith("BID_HISTORY_SUCCESS")) {
                loadBidList(response);
            } else if (response.equals("BID_HISTORY_SUCCESS|EMPTY")) {
                vboxBidList.getChildren().clear();
                vboxBidList.getChildren().add(new Label("Bạn chưa tham gia phiên đấu giá nào."));
            }
        });
    }

    private void loadBidList(String message) {
        vboxBidList.getChildren().clear();
        String[] parts = message.split("\\|");
        // parts[0] = "BID_HISTORY_SUCCESS"
        // từ parts[1] trở đi là các record bid history

        for (int i = 1; i < parts.length; i++) {
            String record = parts[i];
            if (record.isEmpty() || record.equals("EMPTY")) continue;
            String[] data = record.split("#");
            // data: id, product_name, item_type, image_path, current_price, status
            if (data.length >= 6) {
                HBox card = createBidCard(data);
                vboxBidList.getChildren().add(card);
            }
        }
    }

    private HBox createBidCard(String[] data) {
        String auctionId = data[0];
        String productName = data[1];
        String itemType = data[2];
        String imagePath = data[3];
        String currentPrice = data[4];
        String status = data[5];

        HBox card = new HBox(15);
        card.getStyleClass().add("auction-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Ảnh thumbnail
        StackPane imageBox = new StackPane();
        imageBox.setPrefSize(70, 70);
        imageBox.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 8;");
        try {
            if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    ImageView iv = new ImageView(new Image(file.toURI().toString()));
                    iv.setFitWidth(70);
                    iv.setFitHeight(70);
                    iv.setPreserveRatio(true);
                    imageBox.getChildren().add(iv);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        if (imageBox.getChildren().isEmpty()) {
            Label noImg = new Label("📷");
            noImg.setStyle("-fx-font-size: 24px; -fx-text-fill: #6B7280;");
            imageBox.getChildren().add(noImg);
        }

        // Thông tin
        VBox info = new VBox(5);
        Label lblName = new Label(productName);
        lblName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        Label lblType = new Label(itemType);
        lblType.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
        Label lblStatus = new Label();
        lblStatus.getStyleClass().add("status-badge");
        switch (status) {
            case "ACTIVE":
                lblStatus.setText("Đang diễn ra");
                lblStatus.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 2 10;");
                break;
            case "WON":
                lblStatus.setText("Đã thắng");
                lblStatus.setStyle("-fx-background-color: #EAB308; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 2 10; -fx-font-weight: bold;");
                break;
            case "LOST":
                lblStatus.setText("Đã thua");
                lblStatus.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 2 10;");
                break;
            default:
                lblStatus.setText(status);
                lblStatus.setStyle("-fx-background-color: #6B7280; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 2 10;");
        }
        info.getChildren().addAll(lblName, lblType, lblStatus);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Giá hiện tại
        Label lblPrice = new Label(currentPrice + " đ");
        lblPrice.setStyle("-fx-text-fill: #3B82F6; -fx-font-weight: bold; -fx-font-size: 18px;");

        card.getChildren().addAll(imageBox, info, spacer, lblPrice);

        // Sự kiện click
        card.setOnMouseClicked(e -> showBidDetail(data));

        return card;
    }

    private void showBidDetail(String[] data) {
        String auctionId = data[0];
        String productName = data[1];
        String itemType = data[2];
        String imagePath = data[3];
        String currentPrice = data[4];
        String status = data[5];

        this.selectedAuctionId = auctionId;
        this.selectedStatus = status;

        lblDetailName.setText(productName);
        lblDetailCategory.setText(itemType);
        lblDetailStartPrice.setText("..."); // cần thêm dữ liệu từ server nếu muốn
        lblDetailCurrentPrice.setText(currentPrice + " đ");

        // Giá của bạn (tạm thời hiển thị như giá hiện tại, có thể lấy từ server sau)
        lblDetailYourBid.setText(currentPrice + " đ");

        // Ảnh chi tiết
        imgDetailImage.setVisible(false);
        try {
            if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    imgDetailImage.setImage(new Image(file.toURI().toString()));
                    imgDetailImage.setVisible(true);
                }
            }
        } catch (Exception e) {
            // ignore
        }

        // Badge trạng thái
        switch (status) {
            case "ACTIVE":
                lblDetailStatus.setText("Đang diễn ra");
                lblDetailStatus.setStyle("-fx-background-color: #10B981; -fx-text-fill: white;");
                btnEnterRoom.setVisible(true);
                break;
            case "WON":
                lblDetailStatus.setText("Đã thắng");
                lblDetailStatus.setStyle("-fx-background-color: #EAB308; -fx-text-fill: white;");
                btnEnterRoom.setVisible(false);
                break;
            case "LOST":
                lblDetailStatus.setText("Đã thua");
                lblDetailStatus.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;");
                btnEnterRoom.setVisible(false);
                break;
            default:
                lblDetailStatus.setText(status);
                lblDetailStatus.setStyle("-fx-background-color: #6B7280; -fx-text-fill: white;");
                btnEnterRoom.setVisible(false);
        }
    }

    @FXML
    void handleEnterBidRoom(ActionEvent event) {
        if (selectedAuctionId == null) return;
        if ("ACTIVE".equals(selectedStatus)) {
            UserSession.getInstance().setSelectedAuctionId(Integer.parseInt(selectedAuctionId));
            SceneSwitcher.getInstance().switchTo("/fxml/bid_room.fxml", (Node) event.getSource(), "Phòng đấu giá");
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Không thể vào");
            alert.setHeaderText(null);
            alert.setContentText("Phiên đấu giá đã kết thúc.");
            alert.showAndWait();
        }
    }

    private void filterBidList() {
        String filter = cbStatusFilter.getValue();
        vboxBidList.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox card = (HBox) node;
                // Card có lưu trạng thái trong label thứ 2 của VBox info
                VBox info = (VBox) card.getChildren().get(1);
                Label statusLabel = (Label) info.getChildren().get(2);
                String status = statusLabel.getText();
                boolean show = false;
                switch (filter) {
                    case "Tất cả":
                        show = true;
                        break;
                    case "Đang diễn ra":
                        show = "Đang diễn ra".equals(status);
                        break;
                    case "Đã thắng":
                        show = "Đã thắng".equals(status);
                        break;
                    case "Đã thua":
                        show = "Đã thua".equals(status);
                        break;
                }
                card.setVisible(show);
                card.setManaged(show);
            }
        });
    }

    // Điều hướng sidebar
    @FXML void handleGoToAccount(ActionEvent e)  { switchTo("/fxml/account.fxml", e); }
    @FXML void handleGoToAuction(ActionEvent e)  { switchTo("/fxml/main_auction.fxml", e); }
    @FXML void handleGoToProducts(ActionEvent e) { switchTo("/fxml/product_management.fxml", e); }
    @FXML void handleGoToHelp(ActionEvent e)    { switchTo("/fxml/help.fxml", e); }

    private void switchTo(String fxml, ActionEvent e) {
        SceneSwitcher.getInstance().switchTo(fxml, (Node) e.getSource(), "");
    }
}