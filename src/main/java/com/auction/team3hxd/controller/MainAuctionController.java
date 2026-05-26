package com.auction.team3hxd.controller;

import com.auction.team3hxd.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

import com.auction.team3hxd.util.SceneSwitcher;
import javafx.scene.Node;

public class
MainAuctionController {

  // === KHU VỰC TRÁI: DANH SÁCH ===
  @FXML
  private TextField txtSearch;
  @FXML
  private ComboBox<String> cbCategoryFilter;
  @FXML
  private VBox vboxAuctionList;

  // === KHU VỰC PHẢI: CHI TIẾT ===
  @FXML
  private ImageView imgProductDetail;
  @FXML
  private Label lblDetailName;
  @FXML
  private Label lblDetailStatus;
  @FXML
  private Label lblDetailSeller;
  @FXML
  private Label lblDetailCategory;
  @FXML
  private Label lblDetailStartPrice;
  @FXML
  private Label lblDetailBidCount;
  @FXML
  private Label lblDetailDesc;
  @FXML
  private Label lblDetailCurrentPrice;
  @FXML
  private Label lblDetailTimeLeft;
  @FXML
  private Button btnPlaceBid;

  // Lưu trữ ID của phiên đấu giá đang được chọn để truyền sang phòng Bid
  private String selectedAuctionId = null;
  private String selectedSellerId = null;

  // Lớp DTO nội bộ để giữ dữ liệu cho dễ thao tác
  private class AuctionSessionDTO {

    String id, itemName, category, sellerName, sellerId, startPrice, currentPrice, bidCount, timeLeft, desc, imagePath;

    public AuctionSessionDTO(String[] data) {
      this.id = data[0];
      this.itemName = data[1];
      this.category = data[2];
      this.sellerName = data[3];
      this.sellerId = data[4]; // Giả sử server gửi thêm sellerId ở vị trí này
      this.startPrice = data[5];
      this.currentPrice = data[6];
      this.bidCount = data[7];
      this.timeLeft = data[8];
      this.desc = data[9];
      this.imagePath = data.length > 10 ? data[10] : "";
    }
  }

  @FXML
  public void initialize() {
    // 1. Cài đặt bộ lọc mẫu
    cbCategoryFilter.getItems().addAll("Tất cả danh mục", "Điện tử", "Phương tiện", "Nghệ thuật");
    cbCategoryFilter.getSelectionModel().selectFirst();

    // 2. Tạm thời vô hiệu hóa nút Đặt giá khi chưa chọn sản phẩm nào
    btnPlaceBid.setDisable(true);
    clearDetailPanel();

    // 3. Đăng ký nhận tin nhắn từ Server và yêu cầu lấy dữ liệu
    com.auction.team3hxd.util.SocketService.getInstance()
        .setMessageHandler(this::handleServerResponse);

    System.out.println(">>> Đang tải danh sách đấu giá LIVE...");
    com.auction.team3hxd.util.SocketService.getInstance().send("GET_LIVE_AUCTIONS");
  }

  private void handleServerResponse(String message) {
    System.out.println(">>> Server trả về: " + message);
    Platform.runLater(() -> {
      if (message.startsWith("LIVE_AUCTIONS_SUCCESS")) {
        loadLiveAuctions(message);
      } else if (message.equals("LIVE_AUCTIONS_EMPTY")) {
        vboxAuctionList.getChildren().clear();
        Label lblEmpty = new Label("Hiện không có phiên đấu giá nào đang diễn ra.");
        lblEmpty.getStyleClass().add("text-muted");
        vboxAuctionList.getChildren().add(lblEmpty);
      } else if (message.startsWith("NEW_AUCTION_ARRIVED")) {
        com.auction.team3hxd.util.SocketService.getInstance().send("GET_LIVE_AUCTIONS");
      }
    });
  }

  private void loadLiveAuctions(String message) {
    vboxAuctionList.getChildren().clear();
    String[] items = message.split("\\|");

    // Duyệt từ 1 vì items[0] là chữ "LIVE_AUCTIONS_SUCCESS"
    for (int i = 1; i < items.length; i++) {
      String[] data = items[i].split("#");
      if (data.length >= 9) { // Đảm bảo server gửi đủ trường
        AuctionSessionDTO auction = new AuctionSessionDTO(data);
        HBox card = createAuctionCardUI(auction);
        vboxAuctionList.getChildren().add(card);
      }
    }
  }

  // Hàm "vẽ" Card sản phẩm bằng JavaFX Code
  private HBox createAuctionCardUI(AuctionSessionDTO auction) {
    HBox card = new HBox(15);
    card.getStyleClass().add("auction-card");

    // Khung ảnh giả (Icon)
    StackPane imageBox = new StackPane();
    imageBox.setPrefSize(70, 70);
    imageBox.getStyleClass().add("image-thumb-placeholder");
    Label lblIcon = new Label("📷");
    lblIcon.setStyle("-fx-text-fill: #64748B; -fx-font-size: 24;");
    imageBox.getChildren().add(lblIcon);

    // Cột giữa: Tên, Tag danh mục, Người bán
    VBox midCol = new VBox(5);
    midCol.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(midCol, Priority.ALWAYS);

    Label lblName = new Label(auction.itemName);
    lblName.getStyleClass().add("text-bold");
    lblName.setWrapText(true);

    HBox tagsBox = new HBox(8);
    tagsBox.setAlignment(Pos.CENTER_LEFT);
    Label lblCat = new Label(auction.category);
    lblCat.getStyleClass().add("category-tag");
    Label lblSeller = new Label("Bởi: " + auction.sellerName);
    lblSeller.getStyleClass().add("text-muted-small");
    tagsBox.getChildren().addAll(lblCat, lblSeller);

    midCol.getChildren().addAll(lblName, tagsBox);

    // Cột phải: Status, Thời gian, Giá
    VBox rightCol = new VBox(5);
    rightCol.setAlignment(Pos.CENTER_RIGHT);

    Label lblStatus = new Label("Đang diễn ra");
    lblStatus.getStyleClass().add("badge-live"); // Hoặc badge-winning tùy logic sau này

    Label lblTime = new Label("⏱ " + auction.timeLeft);
    lblTime.getStyleClass().add("time-small");

    Label lblPrice = new Label("Giá: " + auction.currentPrice + " đ");
    lblPrice.getStyleClass().add("price-small");

    rightCol.getChildren().addAll(lblStatus, lblTime, lblPrice);

    card.getChildren().addAll(imageBox, midCol, rightCol);

    // Xử lý sự kiện CLICK vào Card
    card.setOnMouseClicked(event -> {
      // Xóa hiệu ứng chọn của các card khác
      vboxAuctionList.getChildren()
          .forEach(node -> node.getStyleClass().remove("auction-card-selected"));
      // Thêm viền xanh cho card hiện tại
      card.getStyleClass().add("auction-card-selected");

      // Đẩy dữ liệu sang panel bên phải
      showAuctionDetails(auction);
    });

    return card;
  }

  private void showAuctionDetails(AuctionSessionDTO auction) {
    selectedAuctionId = auction.id;
    selectedSellerId = auction.sellerId;

    lblDetailName.setText(auction.itemName);
    lblDetailStatus.setText("Đang diễn ra");
    lblDetailSeller.setText(auction.sellerName);
    lblDetailCategory.setText(auction.category);
    lblDetailStartPrice.setText(auction.startPrice + " đ");
    lblDetailBidCount.setText(auction.bidCount + " lượt");
    lblDetailDesc.setText(auction.desc);
    lblDetailCurrentPrice.setText(auction.currentPrice + " đ");
    lblDetailTimeLeft.setText(auction.timeLeft);

    // Xử lý hiện ảnh nếu có
    if (auction.imagePath != null && !auction.imagePath.isEmpty()) {
      File file = new File(auction.imagePath);
      if (file.exists()) {
        Image image = new Image(file.toURI().toString());
        imgProductDetail.setImage(image);
        imgProductDetail.setVisible(true);
      }
    }

    // Bật nút Đặt giá
    btnPlaceBid.setDisable(false);
  }

  private void clearDetailPanel() {
    lblDetailName.setText("Chọn một sản phẩm");
    lblDetailStatus.setText("---");
    lblDetailSeller.setText("---");
    lblDetailCategory.setText("---");
    lblDetailStartPrice.setText("0 đ");
    lblDetailBidCount.setText("0 lượt");
    lblDetailDesc.setText(
        "Vui lòng chọn một phiên đấu giá từ danh sách bên trái để xem chi tiết...");
    lblDetailCurrentPrice.setText("0 đ");
    lblDetailTimeLeft.setText("00:00");
    imgProductDetail.setVisible(false);
  }

  @FXML
  void handleGoToBidRoom(ActionEvent event) {
      if (selectedAuctionId == null) {
          return;
      }

    // Lấy ID người dùng hiện tại từ Session
    // (Đảm bảo Captain thay thế bằng class UserSession thực tế của dự án)
    // Ví dụ: String currentUserId = String.valueOf(UserSession.getInstance().getUser().getId());
    String currentUserId = String.valueOf(
        com.auction.team3hxd.util.UserSession.getInstance().getId());

    // Kiểm tra nếu ID người bán bằng với ID người dùng đang đăng nhập
    if (selectedSellerId != null && selectedSellerId.equals(currentUserId)) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("Thông báo hệ thống");
      alert.setHeaderText(null);
      alert.setContentText("Bạn không thể tham gia đặt giá cho sản phẩm của chính mình!");
      alert.showAndWait();
      return;
    }

    // Nếu hợp lệ, tiến hành chuyển trang
    System.out.println(
        ">>> Đang điều hướng sang phòng đặt giá cho Auction ID: " + selectedAuctionId);
    try {
      // Lưu ID vào Session để BidRoomController biết phải load sản phẩm nào
      UserSession.getInstance().setSelectedAuctionId(Integer.parseInt(selectedAuctionId));

      com.auction.team3hxd.util.SceneSwitcher.getInstance().switchTo(
          "/fxml/bid_room.fxml",
          (javafx.scene.Node) event.getSource(),
          "Phòng Đặt Giá - Sàn Đấu Giá"
      );
    } catch (Exception e) {
      System.out.println(">>> Lỗi khi chuyển giao diện: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @FXML
  void handleGoToAccount(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
  }

  @FXML
  void handleGoToProducts(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/product_management.fxml", (Node) event.getSource(), "Quản lý sản phẩm");
  }

  @FXML
  void handleGoToMyBids(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/my_bids.fxml", (Node) event.getSource(), "Bid đang tham gia");
  }

  @FXML
  void handleGoToHelp(ActionEvent event) {
    SceneSwitcher.getInstance().switchTo("/fxml/help.fxml", (Node) event.getSource(), "Trợ giúp");
  }

}
