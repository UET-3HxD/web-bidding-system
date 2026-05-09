package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.AppConfig;
import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        messageLabel.setVisible(false);
        loadingIndicator.setVisible(false);

        if (!AppConfig.isMockEnabled()) {
            // Đăng ký nhận phản hồi từ SocketService
            SocketService.getInstance().setMessageHandler(this::handleServerResponse);
        } else {
            System.out.println("Chạy ở chế độ MOCK (dữ liệu giả)");
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        messageLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.", false);
            return;
        }

        startLoading();

        if (AppConfig.isMockEnabled()) {
            // ---------- Chế độ MOCK (giả lập) ----------
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boolean ok = "admin".equals(username) && "123".equals(password);
                Platform.runLater(() -> {
                    stopLoading();
                    if (ok) {
                        UserSession.getInstance().login(username, "BIDDER");
                        showMessage("Đăng nhập thành công!", true);
                        SceneSwitcher.getInstance().switchTo(
                                "/fxml/auction_list.fxml",
                                loginButton,
                                "Danh sách phiên đấu giá"
                        );
                    } else {
                        showMessage("Sai tên đăng nhập hoặc mật khẩu.", false);
                    }
                });
            }).start();
        } else {
            // ---------- Chế độ thật (gửi qua socket) ----------
            String loginMessage = "LOGIN|" + username + "|" + password;
            SocketService.getInstance().send(loginMessage);
            // Phản hồi sẽ được xử lý trong handleServerResponse
        }
    }

    private void handleServerResponse(String response) {
        stopLoading();
        if (response.startsWith("LOGIN_OK")) {
            String role = "BIDDER";
            if (response.contains("|")) {
                String[] parts = response.split("\\|");
                if (parts.length > 1) role = parts[1];
            }
            String username = usernameField.getText().trim();
            UserSession.getInstance().login(username, role);
            showMessage("Đăng nhập thành công!", true);
            SceneSwitcher.getInstance().switchTo(
                    "/fxml/auction_list.fxml",
                    loginButton,
                    "Danh sách phiên đấu giá"
            );
        } else if(response.startsWith("LOGIN_ERR_USER_NOT_FOUND")) {
            showMessage("Tài khoản không tồn tại.", false);
        } else if (response.startsWith("LOGIN_ERR_INVALID")) {
            showMessage("Sai tên đăng nhập hoặc mật khẩu.", false);
        } else if (response.startsWith("LOGIN_ERR_ALREADY_ONLINE")) {
            showMessage("Tài khoản đang được đăng nhập ở nơi khác.", false);
        } else {
            showMessage("Lỗi không xác định từ server: " + response, false);
        }
    }

    @FXML
    private void handleRegister() {
        // Chỉ cần chuyển màn hình, không cần dừng listener hay đóng socket
        SceneSwitcher.getInstance().switchTo("/fxml/register.fxml", registerButton, "Đăng ký tài khoản");
    }

    private void startLoading() {
        loginButton.setDisable(true);
        loadingIndicator.setVisible(true);
        messageLabel.setVisible(false);
    }

    private void stopLoading() {
        loginButton.setDisable(false);
        loadingIndicator.setVisible(false);
    }

    private void showMessage(String text, boolean isSuccess) {
        messageLabel.setText(text);
        if (isSuccess) {
            messageLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
        messageLabel.setVisible(true);
        if (!isSuccess) {
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> messageLabel.setVisible(false));
            }).start();
        }
    }
}