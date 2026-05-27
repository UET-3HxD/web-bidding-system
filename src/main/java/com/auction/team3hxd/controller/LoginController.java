package com.auction.team3hxd.controller;

import com.auction.team3hxd.util.AppConfig;
import com.auction.team3hxd.util.SceneSwitcher;
import com.auction.team3hxd.util.SocketService;
import com.auction.team3hxd.util.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label messageLabel;
    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        messageLabel.setVisible(false);
        loadingIndicator.setVisible(false);

        if (!AppConfig.isMockEnabled()) {
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
                        UserSession.getInstance().login(username, "mocktest@email.com", "USER");
                        showMessage("Đăng nhập thành công!", true);
                        SceneSwitcher.getInstance().switchTo(
                                "/fxml/account.fxml",
                                loginButton,
                                "Tài khoản"
                        );
                    } else {
                        showMessage("Sai tên đăng nhập hoặc mật khẩu.", false);
                    }
                });
            }).start();
        } else {
            new Thread(() -> {
                try {
                    if (!SocketService.getInstance().isConnected()) {
                        SocketService.getInstance()
                                .connect(AppConfig.getServerHost(), AppConfig.getServerPort());
                    }

                    String loginMessage = "LOGIN|" + username + "|" + password;
                    SocketService.getInstance().send(loginMessage);

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        stopLoading();
                        showMessage("Không thể kết nối đến máy chủ!", false);
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void handleServerResponse(String response) {
        Platform.runLater(() -> {
            if (response.startsWith("INFO|") || response.startsWith("CHAT|")) {
                return;
            }

            stopLoading();
            if (response.startsWith("LOGIN_OK")) {
                String[] parts = response.split("\\|");
                String role = (parts.length > 1) ? parts[1] : "USER";
                String email = (parts.length > 2) ? parts[2] : "";
                String username = usernameField.getText().trim();
                UserSession.getInstance().login(username, email, role);

                showMessage("Đăng nhập thành công!", true);

                if ("ADMIN".equalsIgnoreCase(role)) {
                    SceneSwitcher.getInstance()
                            .switchTo("/fxml/admin_dashboard.fxml", loginButton, "Admin Dashboard");
                } else {
                    SceneSwitcher.getInstance().switchTo("/fxml/account.fxml", loginButton, "Tài khoản");
                }
            } else if (response.startsWith("LOGIN_ERR_USER_NOT_FOUND")) {
                showMessage("Tài khoản không tồn tại.", false);
            } else if (response.startsWith("LOGIN_ERR_INVALID")) {
                showMessage("Sai tên đăng nhập hoặc mật khẩu.", false);
            } else if (response.startsWith("LOGIN_ERR_ALREADY_ONLINE")) {
                showMessage("Tài khoản đang được đăng nhập ở nơi khác.", false);
            } else if (response.startsWith("LOGIN_ERR_BANNED")) {
                showMessage("Tài khoản đã bị cấm.", false);
            } else {
                showMessage("Lỗi không xác định từ server: " + response, false);
            }
        });
    }

    @FXML
    private void handleRegister() {
        SceneSwitcher.getInstance()
                .switchTo("/fxml/register.fxml", registerButton, "Đăng ký tài khoản");
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

        messageLabel.getStyleClass().removeAll("msg-success", "msg-error");
        if (isSuccess) {
            messageLabel.getStyleClass().add("msg-success");
        } else {
            messageLabel.getStyleClass().add("msg-error");
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