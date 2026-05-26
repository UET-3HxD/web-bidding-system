package com.auction.team3HxD.controller;

import com.auction.team3HxD.dto.ProductDTO;
import com.auction.team3HxD.services.ItemService;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;

import com.auction.team3HxD.util.SceneSwitcher;
import javafx.scene.Node;

public class ProductManagementController {

  // --- CÁC COMPONENT TỪ FXML ---

  @FXML
  private Label lblSidebarName;
  @FXML
  private Label lblSidebarAvatar;
  @FXML
  private Label lblProductCount;
  @FXML
  private VBox vboxProductList;

  // Các Panel chuyển đổi
  @FXML
  private VBox paneCreate;
  @FXML
  private VBox paneEdit;

  // Form Tạo mới
  @FXML
  private TextField txtCreateName;
  @FXML
  private ComboBox<String> cbCreateCategory;
  @FXML
  private TextArea txtCreateDesc;
  @FXML
  private TextField txtCreatePrice;

  // Form Chỉnh sửa
  @FXML
  private TextField txtEditName;
  @FXML
  private TextField txtEditPrice;
  @FXML
  private TextArea txtEditDesc;
  @FXML
  private VBox uploadBox;
  @FXML
  private ComboBox<String> cbAuctionDuration;
  // Biến lưu trữ sản phẩm đang được chọn để chỉnh sửa
  private ProductDTO currentEditingProduct = null;
  private String currentImagePath = "";
  private final ItemService itemService = new ItemService();

  @FXML
  public void initialize() {
    // 1. Nạp thông tin Sidebar từ Session
    String username = UserSession.getInstance().getUsername();
    if (username != null) {
      lblSidebarName.setText(username);
      lblSidebarAvatar.setText(username.substring(0, Math.min(username.length(), 2)).toUpperCase());
    }

    // 2. Khởi tạo dữ liệu mẫu cho ComboBox
    cbCreateCategory.getItems().addAll("Điện tử", "Phương tiện", "Nghệ thuật", "Khác");

    paneCreate.setVisible(true);
    paneEdit.setVisible(false);
    if (cbAuctionDuration != null) {
      cbAuctionDuration.getItems()
          .addAll("5 phút", "15 phút", "30 phút", "1 giờ", "12 giờ", "24 giờ");
    }
    // 3. ĐĂNG KÝ NGƯỜI NGHE VÀ LẤY DỮ LIỆU THẬT
    com.auction.team3HxD.util.SocketService.getInstance()
        .setMessageHandler(this::handleServerResponse);
    com.auction.team3HxD.util.SocketService.getInstance().send("GET_MY_ITEMS");
  }

  private void handleServerResponse(String message) {
    Platform.runLater(() -> {
      System.out.println(">>> Controller nhận được: " + message);
      if (message.equals("CREATE_ITEM_SUCCESS")) {
        System.out.println("Tạo sản phẩm thành công!");
        txtCreateName.clear();
        txtCreatePrice.clear();
        txtCreateDesc.clear();
        currentImagePath = "";
        uploadBox.getChildren().clear(); // Xóa ảnh preview

        // Tải lại danh sách
        com.auction.team3HxD.util.SocketService.getInstance().send("GET_MY_ITEMS");
        showAlert("Thành công", "Đăng kí thành công! Sản phẩm đang chờ quản trị viên phê duyệt.",
            Alert.AlertType.INFORMATION);
        resetUploadUI();

      } else if (message.startsWith("CREATE_ITEM_ERR")) {
        String reason = message.contains("|") ? message.split("\\|")[1] : "Lỗi không xác định";
        System.err.println("Thất bại: " + reason);

      }
      else if (message.startsWith("LIST_ITEMS_SUCCESS")) {
        // XỬ LÝ CHUỖI DỮ LIỆU TỪ SERVER VÀ VẼ UI
        loadRealProducts(message);

      } else if (message.equals("LIST_ITEMS_EMPTY")) {
        System.out.println(">>> Thông báo: Bạn chưa có sản phẩm nào trong DB.");
        vboxProductList.getChildren().clear();
        lblProductCount.setText("0 sản phẩm");
      } else if (message.equals("UPDATE_ITEM_SUCCESS")) {
        showAlert("Thành công",
            "Đã cập nhật sản phẩm thành công! Sản phẩm đang chờ admin duyệt lại.",
            Alert.AlertType.INFORMATION);
        handleCloseEdit(null);
        System.out.println(
            ">>> [UI] Đang tải lại danh sách sản phẩm của tôi để cập nhật tag 'Chờ duyệt'...");
        com.auction.team3HxD.util.SocketService.getInstance().send("GET_MY_ITEMS");
      } else if (message.equals("DELETE_ITEM_SUCCESS")) {
        showAlert("Thành công", "Đã xóa sản phẩm khỏi hệ thống!", Alert.AlertType.INFORMATION);
        handleCloseEdit(null); // Đóng panel chỉnh sửa và reset form
        com.auction.team3HxD.util.SocketService.getInstance()
            .send("GET_MY_ITEMS"); // Tải lại danh sách
      } else if (message.startsWith("DELETE_ITEM_ERR")) {
        showAlert("Lỗi", "Không thể xóa sản phẩm lúc này.", Alert.AlertType.ERROR);
      } else if (message.equals("START_AUCTION_SUCCESS")) {
        showAlert("Lên sàn thành công!",
            "Phiên đấu giá đã chính thức bắt đầu và được hiển thị ở Khu vực chính.",
            Alert.AlertType.INFORMATION);

        handleCloseEdit(null);

        com.auction.team3HxD.util.SocketService.getInstance().send("GET_MY_ITEMS");

      } else if (message.startsWith("START_AUCTION_ERR")) {
        String errorMsg = message.contains("|") ? message.split("\\|")[1] : "Lỗi không xác định";
        showAlert("Không thể tạo phiên đấu giá", errorMsg, Alert.AlertType.ERROR);
      } else if (message.startsWith("MY_PRODUCT_STATUS_CHANGED")) {
        javafx.application.Platform.runLater(() -> {
          String[] parts = message.split("\\|");
          if (parts.length >= 3) {
            String status = parts[2];
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                status.equals("APPROVED") ? javafx.scene.control.Alert.AlertType.INFORMATION
                    : javafx.scene.control.Alert.AlertType.WARNING
            );
            alert.setTitle("Cập nhật trạng thái");
            alert.setHeaderText(null);

            if (status.equals("APPROVED")) {
              alert.setContentText("Sản phẩm của bạn đã được Admin duyệt thành công!");
            } else {
              alert.setContentText("Sản phẩm của bạn đã bị từ chối.");
            }
            alert.show();
            SocketService.getInstance().send("GET_MY_ITEMS");
            System.out.println(">>> [REAL-TIME USER] Đã cập nhật lại bảng vì Admin vừa thao tác.");
          }
        });
      } else if (message.startsWith("AUCTION_ENDED")) {
        SocketService.getInstance().send("GET_MY_ITEMS");
        System.out.println(">>> [REAL-TIME] Phiên đấu giá kết thúc. cập nhật lại danh sách...");
      }
    });
  }

  private void loadRealProducts(String message) {
    vboxProductList.getChildren().clear();
    String[] parts = message.split("\\|");
    int count = 0;

    // Bắt đầu từ 1 vì parts[0] là "LIST_ITEMS_SUCCESS"
    for (int i = 1; i < parts.length; i++) {
      String[] itemData = parts[i].split("#");

      // Đảm bảo server gửi đủ 6 trường: id#name#price#status#path#desc
      if (itemData.length >= 6) {
        String id = itemData[0];
        String name = itemData[1];
        String price = itemData[2];
        String status = itemData[3];
        String path = itemData[4];
        String desc = itemData[5];

        ProductDTO p = new ProductDTO(id, name, price, desc, status, path);
        HBox card = createProductCardUI(p);
        vboxProductList.getChildren().add(card);
        count++;
      }
    }
    lblProductCount.setText(count + " sản phẩm");
  }

  @FXML
  void handleUpdateProduct(ActionEvent event) {
    if (currentEditingProduct == null) {
      return;
    }

    // 1. Kiểm tra trạng thái (Chỉ cho phép sửa nếu là WAITING hoặc đã duyệt nhưng chưa LIVE)
    String status = currentEditingProduct.getStatus();
    if (status.equals("LIVE") || status.equals("SOLD")) {
      showAlert("Thông báo",
          "Sản phẩm đang trong phiên đấu giá hoặc đã kết thúc, không thể chỉnh sửa!",
          Alert.AlertType.WARNING);
      return;
    }

    // 2. Lấy dữ liệu mới từ Form
    String newName = txtEditName.getText().trim();
    String newPrice = txtEditPrice.getText().trim();
    String newDesc = txtEditDesc.getText().trim();

    if (!itemService.itemValidator(newName, newPrice, newDesc, "UNCHANGED")){
      showAlert("Thông báo", "Thông tin chỉnh sửa không hợp lệ", Alert.AlertType.INFORMATION);
      return;
    }
    // 3. So sánh với thông tin cũ
    boolean isChanged = !newName.equals(currentEditingProduct.getName()) ||
        !newPrice.equals(currentEditingProduct.getPrice()) ||
        !newDesc.equals(currentEditingProduct.getDescription());

    if (!isChanged) {
      showAlert("Thông báo", "Thông tin không có gì thay đổi!", Alert.AlertType.INFORMATION);
      return;
    }

    // 4. Gửi lệnh cập nhật kèm ID sản phẩm
    // Cấu trúc: UPDATE_ITEM|id|name|price|desc
    String message = String.format("UPDATE_ITEM|%s|%s|%s|%s",
        currentEditingProduct.getId(), newName, newPrice, newDesc);
    currentEditingProduct.setStatus("WAITING");
    com.auction.team3HxD.util.SocketService.getInstance().send(message);
    System.out.println(">>> Đã gửi yêu cầu cập nhật Item ID: " + currentEditingProduct.getId());
  }

  // --- CẬP NHẬT GIAO DIỆN CARD ---
  private HBox createProductCardUI(ProductDTO product) {
    HBox card = new HBox(15);
    card.getStyleClass().add("product-card-item");
    card.setAlignment(Pos.CENTER_LEFT);

    // Hiển thị ảnh thật từ đường dẫn (Path)
    ImageView imageView = new ImageView();
    imageView.setFitWidth(60);
    imageView.setFitHeight(60);
    imageView.setPreserveRatio(true);

    try {
      if (product.getImagePath() != null && !product.getImagePath().equals("null")
          && !product.getImagePath().isEmpty()) {
        File file = new File(product.getImagePath());
        if (file.exists()) {
          imageView.setImage(new Image(file.toURI().toString()));
        }
      }
    } catch (Exception e) {
      System.err.println("Không thể tải ảnh cho sản phẩm: " + product.getName());
    }

    // Bọc ảnh vào một StackPane để tạo khung vuông vức
    StackPane imgContainer = new StackPane(imageView);
    imgContainer.setMinSize(60, 60);
    imgContainer.setMaxSize(60, 60);
    imgContainer.getStyleClass().add("product-image-box");

    // Cột thông tin
    VBox infoBox = new VBox(5);
    Label lblName = new Label(product.getName());
    lblName.getStyleClass().add("product-name-text");

    Label lblStatusTag = new Label();

    // Logic phân loại Tag dựa trên trạng thái
    switch (product.getStatus()) {
      case "WAITING":
        lblStatusTag.setText("Chờ duyệt");
        lblStatusTag.getStyleClass().setAll("status-badge", "badge-waiting");
        break;

      case "APPROVED":
        lblStatusTag.setText("Đã duyệt");
        lblStatusTag.getStyleClass().setAll("status-badge", "badge-approved");
        break;

      case "LIVE":
        lblStatusTag.setText("Đang sàn");
        lblStatusTag.getStyleClass().setAll("status-badge", "badge-live");
        break;

      case "SOLD":
        lblStatusTag.setText("Đã đóng");
        lblStatusTag.getStyleClass().setAll("status-badge", "badge-sold");
        break;

      case "REJECTED":
        lblStatusTag.setText("Bị từ chối");
        lblStatusTag.getStyleClass().setAll("status-badge", "badge-rejected");
        break;

      default:
        lblStatusTag.setText(product.getStatus());
        lblStatusTag.getStyleClass().setAll("status-badge", "badge-waiting");
    }

    infoBox.getChildren().addAll(lblName, lblStatusTag);

    // Cột Giá
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Format giá tiền (VD: 500000.0 -> 500,000)
    String displayPrice = product.getPrice().replace(".0", "");
    Label lblPrice = new Label(displayPrice + " đ");
    lblPrice.getStyleClass().add("product-price-text");

    card.getChildren().addAll(imgContainer, infoBox, spacer, lblPrice);
    card.setOnMouseClicked(e -> handleProductSelect(product));

    return card;
  }

  /**
   * Hàm này được gọi khi Click vào nút "X" trên Panel Chỉnh Sửa
   */
  @FXML
  void handleCloseEdit(ActionEvent event) {
    // Ẩn form sửa, hiện form tạo
    paneEdit.setVisible(false);
    paneCreate.setVisible(true);

    // Xóa sạch dữ liệu cũ
    currentEditingProduct = null;
    txtEditName.clear();
    txtEditPrice.clear();
    txtEditDesc.clear();
  }

  /**
   * Hàm này được gọi khi Click vào một Card Sản phẩm ở danh sách bên trái
   */
  private void handleProductSelect(ProductDTO product) {
    // 1. Lưu lại sản phẩm đang thao tác
    currentEditingProduct = product;

    // 2. Bơm dữ liệu vào Form Edit
    txtEditName.setText(product.getName());
    txtEditPrice.setText(String.valueOf(product.getPrice()));
    txtEditDesc.setText(product.getDescription());

    // 3. Hiệu ứng "Swap": Ẩn form tạo, hiện form sửa
    paneCreate.setVisible(false);
    paneEdit.setVisible(true);
  }

  // --- CÁC HÀM XỬ LÝ CHÍNH (GỬI LÊN SERVER SAU NÀY) ---

  @FXML
  void handleCreateProduct(ActionEvent event) {
    // 1. Lấy dữ liệu từ UI
    String name = txtCreateName.getText().trim();
    String priceStr = txtCreatePrice.getText().trim();
    String desc = txtCreateDesc.getText().trim();
    String category = cbCreateCategory.getValue(); // Ví dụ: "Điện tử", "Xe cộ", "Nghệ thuật"

    if (!itemService.itemValidator(name, priceStr, desc, category)) {
      showAlert("Thông báo", "Thông tin sản phẩm không hợp lệ!", Alert.AlertType.INFORMATION);
      return;
    }

    String type;
    switch (category) {
      case "Điện tử":
        type = "ELECTRONIC";
        break;
      case "Phương tiện":
        type = "VEHICLE";
        break;
      case "Nghệ thuật":
        type = "ART";
        break;
      case "Khác":
        type = "OTHER";
        break;
      default:
        type = "ELECTRONIC";
    }

    // 4. Gửi lệnh qua Socket (Sử dụng đường dẫn ảnh đã lưu từ handleUploadImage)
    // Cấu trúc: CREATE_ITEM|tên|giá|loại|mô tả|đường dẫn ảnh
    String message = String.format("CREATE_ITEM|%s|%s|%s|%s|%s",
        name, priceStr, type, desc, currentImagePath);

    com.auction.team3HxD.util.SocketService.getInstance().send(message);
    System.out.println(">>> Đã gửi yêu cầu tạo sản phẩm: " + name);
  }

  private void resetUploadUI() {
    uploadBox.getChildren().clear();

    // Tạo lại Icon (Bạn có thể dùng Label chứa emoji hoặc ImageView)
    Label iconLabel = new Label("📤");
    iconLabel.getStyleClass().add("upload-icon-label");

    Label mainText = new Label("Tải ảnh sản phẩm");
    mainText.getStyleClass().add("upload-main-text");

    Label subText = new Label("PNG, JPG tối đa 5MB");
    subText.getStyleClass().add("upload-sub-text");

    uploadBox.getChildren().addAll(iconLabel, mainText, subText);
    currentImagePath = ""; // Reset đường dẫn ảnh
  }

  // Hàm hiển thị thông báo nhanh cho Captain
  private void showAlert(String title, String content, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  @FXML
  void handleDeleteProduct(ActionEvent event) {
    if (currentEditingProduct == null) {
      return;
    }

    // 1. Kiểm tra trạng thái (Chỉ cho phép xóa nếu là WAITING)
    String status = currentEditingProduct.getStatus();
    if (status.equals("LIVE") || status.equals("SOLD")) {
      showAlert("Thông báo", "Sản phẩm đang đấu giá hoặc đã kết thúc, không thể xóa!",
          Alert.AlertType.WARNING);
      return;
    }

    // 2. Hiện hộp thoại xác nhận (Confirm)
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Xác nhận xóa");
    confirmAlert.setHeaderText("Bạn có chắc chắn muốn xóa sản phẩm này?");
    confirmAlert.setContentText(
        "Hành động này không thể hoàn tác: " + currentEditingProduct.getName());

    // Chờ người dùng phản hồi
    confirmAlert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        // 3. Gửi lệnh xóa kèm ID sản phẩm qua Socket
        // Cấu trúc: DELETE_ITEM|id
        String message = "DELETE_ITEM|" + currentEditingProduct.getId();
        com.auction.team3HxD.util.SocketService.getInstance().send(message);
        System.out.println(">>> Đã gửi yêu cầu XÓA Item ID: " + currentEditingProduct.getId());
      }
    });
  }

  @FXML
  void handleUploadImage(MouseEvent event) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Chọn ảnh sản phẩm");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
    );

    File selectedFile = fileChooser.showOpenDialog(uploadBox.getScene().getWindow());

    if (selectedFile != null) {
      // Lấy đường dẫn tuyệt đối của file trên máy tính
      this.currentImagePath = selectedFile.getAbsolutePath();

      // Hiển thị xem trước (Preview)
      Image img = new Image(selectedFile.toURI().toString());
      ImageView imageView = new ImageView(img);
      imageView.setFitWidth(150);
      imageView.setFitHeight(120);
      imageView.setPreserveRatio(true);

      uploadBox.getChildren().clear();
      uploadBox.getChildren().add(imageView);

      System.out.println(">>> Đã lấy đường dẫn ảnh: " + currentImagePath);
    }
  }

  /**
   * Hàm này "vẽ" ra một cái Card sản phẩm bằng code Java thay vì FXML
   */

  @FXML
  void handlePublishAuction(ActionEvent event) {
    if (currentEditingProduct == null) {
      return;
    }

    // 1. Kiểm tra trạng thái: Phải là APPROVED (đã được duyệt) mới được đăng
    String status = currentEditingProduct.getStatus();
    if (status.equals("WAITING")) {
      showAlert("Thông báo", "Sản phẩm đang chờ Admin duyệt, chưa thể đăng lên sàn!",
          Alert.AlertType.WARNING);
      return;
    } else if (status.equals("LIVE") || status.equals("SOLD")) {
      showAlert("Thông báo", "Sản phẩm này đã ở trên sàn hoặc đã bán!", Alert.AlertType.WARNING);
      return;
    } else if (status.equals("REJECTED")) {
      showAlert("Thông báo",
          "Sản phẩm đã bị từ chối bởi admin! Vui lòng chỉnh sửa hoặc tạo mới sản phẩm phù hợp với quy định.",
          Alert.AlertType.WARNING);
      return;
    }

    // 2. Kiểm tra xem đã chọn thời gian chưa
    String durationStr = cbAuctionDuration.getValue();
    if (durationStr == null) {
      showAlert("Lỗi", "Vui lòng chọn thời lượng diễn ra đấu giá!", Alert.AlertType.ERROR);
      return;
    }

    // 3. Hiện Alert Confirm
    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
    confirmAlert.setTitle("Xác nhận đăng sàn");
    confirmAlert.setHeaderText("Mở phiên đấu giá cho: " + currentEditingProduct.getName());
    confirmAlert.setContentText(
        "Sản phẩm sẽ hiển thị trên Khu vực chính với thời gian là " + durationStr
            + ". Bạn có chắc chắn?");

    confirmAlert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        // Chuyển đổi chuỗi thành số phút để gửi lên Server
        int minutes = parseDurationToMinutes(durationStr);

        // Gửi lệnh qua Socket: START_AUCTION|itemId|minutes
        String message = "START_AUCTION|" + currentEditingProduct.getId() + "|" + minutes;
        com.auction.team3HxD.util.SocketService.getInstance().send(message);
        System.out.println(">>> Đã gửi lệnh bắt đầu đấu giá: " + message);
      }
    });
  }

  // Hàm phụ trợ chuyển chuỗi "15 phút" thành số int 15
  private int parseDurationToMinutes(String durationStr) {
    switch (durationStr) {
      case "5 phút":
        return 5;
      case "15 phút":
        return 15;
      case "30 phút":
        return 30;
      case "1 giờ":
        return 60;
      case "12 giờ":
        return 720;
      case "24 giờ":
        return 1440;
      default:
        return 60;
    }
  }

  @FXML
  void handleGoToAccount(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
  }

  @FXML
  void handleGoToAuction(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/main_auction.fxml", (Node) event.getSource(), "Sàn đấu giá");
  }

  // Thêm
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