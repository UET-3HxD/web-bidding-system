package com.auction.team3hxd.controller;

import com.auction.team3hxd.util.SceneSwitcher;
import com.auction.team3hxd.util.SocketService;
import com.auction.team3hxd.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class AdminManageUsersController {

    @FXML
    private Label lblSidebarName;
    @FXML
    private Label lblSidebarAvatar;
    @FXML
    private Label lblUserCount;
    @FXML
    private VBox vboxUserList;

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username != null) {
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(2, username.length())).toUpperCase();
            lblSidebarAvatar.setText(shortName);
        }

        SocketService.getInstance().setMessageHandler(this::handleServerResponse);
        SocketService.getInstance().send("GET_ALL_USERS");
    }

    private void handleServerResponse(String response) {
        Platform.runLater(() -> {
            if (response.startsWith("ALL_USERS")) {
                loadUserList(response);
            } else if (response.equals("ALL_USERS_EMPTY")) {
                vboxUserList.getChildren().clear();
                vboxUserList.getChildren().add(new Label("Không có người dùng nào."));
                lblUserCount.setText("0 người dùng");
            } else if (response.equals("ADMIN_BAN_SUCCESS|Đã khóa tài khoản thành công.")) {
                showAlert("Thành công", "Đã khóa tài khoản!", Alert.AlertType.INFORMATION);
                SocketService.getInstance().send("GET_ALL_USERS"); // reload
            } else if (response.equals("UNBAN_SUCCESS")) {
                showAlert("Thành công", "Đã mở khóa tài khoản!", Alert.AlertType.INFORMATION);
                SocketService.getInstance().send("GET_ALL_USERS"); // reload
            } else if (response.startsWith("ADMIN_BAN_ERROR") || response.startsWith("UNBAN_ERR")) {
                String msg = response.contains("|") ? response.split("\\|")[1] : "Lỗi không xác định";
                showAlert("Lỗi", msg, Alert.AlertType.ERROR);
            }
        });
    }

    private void loadUserList(String message) {
        vboxUserList.getChildren().clear();
        String[] parts = message.split("\\|");
        int count = 0;

        for (int i = 1; i < parts.length; i++) {
            String[] data = parts[i].split("#");
            if (data.length >= 4) {
                String id = data[0];
                String username = data[1];
                String email = data[2];
                String role = data[3];

                HBox card = createUserCard(id, username, email, role);
                vboxUserList.getChildren().add(card);
                count++;
            }
        }
        lblUserCount.setText(count + " người dùng");
    }

    private HBox createUserCard(String id, String username, String email, String role) {
        HBox card = new HBox(15);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Avatar
        Circle avatarCircle = new Circle(20);
        avatarCircle.getStyleClass().add("user-avatar-circle");

        Label avatarLabel = new Label(
                username.substring(0, Math.min(2, username.length())).toUpperCase());
        avatarLabel.getStyleClass().add("user-avatar-text");

        StackPane avatarStack = new StackPane(avatarCircle, avatarLabel);
        avatarStack.setMinWidth(40);
        avatarStack.setMinHeight(40);

        // Thông tin
        VBox info = new VBox(5);
        Label lblUsername = new Label(username);
        lblUsername.getStyleClass().add("user-name-text");

        Label lblEmail = new Label(email);
        lblEmail.getStyleClass().add("user-email-text");

        Label lblRole = new Label(role);
        lblRole.getStyleClass().add("user-role-text");

        info.getChildren().addAll(lblUsername, lblEmail, lblRole);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nút hành động
        VBox actions = new VBox(8);
        actions.setAlignment(Pos.CENTER);

        if (!"ADMIN".equalsIgnoreCase(role)) {
            Button btnBan = new Button("🔒 Khóa");
            btnBan.getStyleClass().add("btn-ban-user");
            btnBan.setOnAction(e -> handleBanUser(id));

            Button btnUnban = new Button("🔓 Mở khóa");
            btnUnban.getStyleClass().add("btn-unban-user");
            btnUnban.setOnAction(e -> handleUnbanUser(id));

            if ("BANNED".equalsIgnoreCase(role)) {
                actions.getChildren().add(btnUnban);
            } else {
                actions.getChildren().add(btnBan);
            }
        } else {
            Label adminBadge = new Label("Quản trị viên");
            adminBadge.getStyleClass().add("admin-badge-text");
            actions.getChildren().add(adminBadge);
        }

        card.getChildren().addAll(avatarStack, info, spacer, actions);
        return card;
    }

    private void handleBanUser(String userId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận khóa");
        confirm.setHeaderText("Khóa người dùng #" + userId + "?");
        confirm.setContentText("Người dùng sẽ bị đăng xuất và không thể đăng nhập lại.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                SocketService.getInstance().send("BAN_USER|" + userId);
            }
        });
    }

    private void handleUnbanUser(String userId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận mở khóa");
        confirm.setHeaderText("Mở khóa người dùng #" + userId + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                SocketService.getInstance().send("UNBAN_USER|" + userId);
            }
        });
    }

    // Điều hướng sidebar
    @FXML
    void handleGoToAccount(ActionEvent e) {
        switchTo("/fxml/account.fxml", e);
    }

    @FXML
    void handleGoToDashboard(ActionEvent e) {
        switchTo("/fxml/admin_dashboard.fxml", e);
    }

    @FXML
    void handleGoToApprove(ActionEvent e) {
        switchTo("/fxml/admin_approve_products.fxml", e);
    }

    @FXML
    void handleGoToHelp(ActionEvent e) {
        switchTo("/fxml/help.fxml", e);
    }

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