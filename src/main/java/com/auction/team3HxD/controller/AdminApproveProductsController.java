package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;

public class AdminApproveProductsController {

    @FXML private Label lblSidebarName, lblSidebarAvatar, lblPendingCount;
    @FXML private VBox vboxProductList;

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username != null) {
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(username.length(), 2)).toUpperCase();
            lblSidebarAvatar.setText(shortName);
        }

        SocketService.getInstance().setMessageHandler(this::handleServerResponse);
        SocketService.getInstance().send("GET_PENDING_ITEMS");
    }

    private void handleServerResponse(String message) {
        Platform.runLater(() -> {
            System.out.println(">>> Admin Approve nhận: " + message);
            if (message.startsWith("PENDING_ITEMS_SUCCESS")) {
                loadPendingProducts(message);
            } else if (message.equals("PENDING_ITEMS_EMPTY")) {
                vboxProductList.getChildren().clear();
                vboxProductList.getChildren().add(new Label("Không có sản phẩm nào chờ duyệt."));
                lblPendingCount.setText("0 sản phẩm chờ duyệt");
            } else if (message.equals("APPROVE_SUCCESS") || message.equals("REJECT_SUCCESS")) {
                showAlert("Thành công", "Thao tác thành công!", Alert.AlertType.INFORMATION);
                SocketService.getInstance().send("GET_PENDING_ITEMS"); // Reload
            } else if (message.startsWith("APPROVE_ERR") || message.startsWith("REJECT_ERR")) {
                String err = message.contains("|") ? message.split("\\|")[1] : "Lỗi không xác định";
                showAlert("Lỗi", err, Alert.AlertType.ERROR);
            }
        });
    }

    private void loadPendingProducts(String message) {
        vboxProductList.getChildren().clear();
        String[] parts = message.split("\\|");
        int count = 0;

        for (int i = 1; i < parts.length; i++) {
            String[] data = parts[i].split("#");
            // id#name#price#sellerName#desc#imagePath
            if (data.length >= 6) {
                vboxProductList.getChildren().add(createPendingCard(data));
                count++;
            }
        }
        lblPendingCount.setText(count + " sản phẩm chờ duyệt");
    }

    private HBox createPendingCard(String[] data) {
        String id = data[0];
        String name = data[1];
        String price = data[2];
        String seller = data[3];
        String desc = data[4];
        String imagePath = data.length > 5 ? data[5] : "";

        HBox card = new HBox(15);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Ảnh
        StackPane imageBox = new StackPane();
        imageBox.setMinSize(80, 80);
        imageBox.setMaxSize(80, 80);
        imageBox.setStyle("-fx-background-color: #1F2937; -fx-background-radius: 8;");

        try {
            if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
                File file = new File(imagePath);
                if (file.exists()) {
                    ImageView iv = new ImageView(new Image(file.toURI().toString()));
                    iv.setFitWidth(80);
                    iv.setFitHeight(80);
                    iv.setPreserveRatio(true);
                    imageBox.getChildren().add(iv);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        if (imageBox.getChildren().isEmpty()) {
            Label noImg = new Label("📷");
            noImg.setStyle("-fx-font-size: 28px; -fx-text-fill: #6B7280;");
            imageBox.getChildren().add(noImg);
        }

        // Thông tin
        VBox info = new VBox(5);
        Label lblName = new Label(name);
        lblName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        Label lblSeller = new Label("Người bán: " + seller);
        lblSeller.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13px;");
        Label lblPrice = new Label(price.replace(".0", "") + " đ");
        lblPrice.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        lblDesc.setWrapText(true);
        lblDesc.setMaxWidth(350);
        info.getChildren().addAll(lblName, lblSeller, lblPrice, lblDesc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nút hành động
        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER);

        Button btnApprove = new Button("✅ Duyệt");
        btnApprove.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnApprove.setOnAction(e -> handleApprove(id));

        Button btnReject = new Button("❌ Từ chối");
        btnReject.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnReject.setOnAction(e -> handleReject(id));

        actions.getChildren().addAll(btnApprove, btnReject);

        card.getChildren().addAll(imageBox, info, spacer, actions);
        return card;
    }

    private void handleApprove(String id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Duyệt sản phẩm #" + id + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                SocketService.getInstance().send("APPROVE_ITEM|" + id);
            }
        });
    }

    private void handleReject(String id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Từ chối sản phẩm #" + id + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                SocketService.getInstance().send("REJECT_ITEM|" + id);
            }
        });
    }

    // Điều hướng
    @FXML void handleGoToAccount(ActionEvent e)  { switchTo("/fxml/account.fxml", e); }
    @FXML void handleGoToDashboard(ActionEvent e) { switchTo("/fxml/admin_dashboard.fxml", e); }
    @FXML void handleGoToUsers(ActionEvent e)     { switchTo("/fxml/admin_manage_users.fxml", e); }
    @FXML void handleGoToHelp(ActionEvent e)     { switchTo("/fxml/help.fxml", e); }

    private void switchTo(String fxml, ActionEvent e) {
        SceneSwitcher.getInstance().switchTo(fxml, (Node) e.getSource(), "");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}