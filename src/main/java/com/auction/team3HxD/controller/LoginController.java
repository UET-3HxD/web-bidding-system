package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.AppConfig;
import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketManager;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * Điều khiển màn hình đăng nhập.
 * Hỗ trợ cả chế độ mock (dữ liệu giả, không cần server) và kết nối thật qua socket.
 * Chế độ được cấu hình trong application.properties (app.mock.enabled).
 */
public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private AnchorPane loginRoot;

    // Luồng lắng nghe phản hồi từ server (chỉ dùng khi không mock)
    private Thread listenerThread;
    private volatile boolean running = true;

    @FXML
    public void initialize() {
        // Ẩn thông báo và loading lúc đầu
        messageLabel.setVisible(false);
        loadingIndicator.setVisible(false);

        // Nếu không dùng mock, kết nối socket và bắt đầu lắng nghe
        if (!AppConfig.isMockEnabled()) {
            connectToServer();
        } else {
            System.out.println("Chạy ở chế độ MOCK (dữ liệu giả)");
        }
    }

    /**
     * Kết nối đến server (chạy trong luồng riêng)
     */
    private void connectToServer() {
        new Thread(() -> {
            try {
                SocketManager.getInstance().connect();     // dùng host/port từ AppConfig
                startListening();
                Platform.runLater(() -> System.out.println("Đã kết nối server thành công"));
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showMessage("Không thể kết nối đến server. Vui lòng thử lại sau.", false);
                    loginButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Bắt đầu luồng lắng nghe phản hồi từ server (chạy nền)
     */
    private void startListening() {
        listenerThread = new Thread(() -> {
            while (running) {
                try {
                    String response = SocketManager.getInstance().receive();
                    if (response != null) {
                        final String res = response;
                        Platform.runLater(() -> handleServerResponse(res));
                    }
                } catch (IOException e) {
                    if (running) {
                        Platform.runLater(() -> showMessage("Mất kết nối đến server.", false));
                        break;
                    }
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Reset thông báo
        messageLabel.setVisible(false);

        // Kiểm tra rỗng
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.", false);
            return;
        }

        // Bắt đầu hiệu ứng loading
        startLoading();

        if (AppConfig.isMockEnabled()) {
            // ---------- Chế độ MOCK (giả lập) ----------
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // giả lập độ trễ mạng
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Tài khoản mock cố định: admin / 123
                boolean ok = "admin".equals(username) && "123".equals(password);
                final boolean finalOk = ok;
                Platform.runLater(() -> {
                    stopLoading();
                    if (finalOk) {
                        // Lưu phiên người dùng
                        UserSession.getInstance().login(username, "BIDDER");
                        showMessage("Đăng nhập thành công!", true);
                        // Chuyển sang màn hình danh sách đấu giá
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
            SocketManager.getInstance().send(loginMessage);
            // Phản hồi sẽ được xử lý trong handleServerResponse
        }
    }

    /**
     * Xử lý phản hồi từ server (chỉ dùng khi không mock)
     * @param response chuỗi phản hồi (theo giao thức)
     */
    private void handleServerResponse(String response) {
        stopLoading();
        if (response.startsWith("LOGIN_OK")) {
            // Có thể server gửi kèm role: LOGIN_OK|SELLER
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
        } else if (response.startsWith("LOGIN_ERR_WRONG_CREDENTIALS")) {
            showMessage("Sai tên đăng nhập hoặc mật khẩu.", false);
        } else if (response.startsWith("LOGIN_ERR_ALREADY_ONLINE")) {
            showMessage("Tài khoản đang được đăng nhập ở nơi khác.", false);
        } else {
            showMessage("Lỗi không xác định từ server: " + response, false);
        }
    }

    @FXML
    private void handleRegister() {
        // Tạm thời thông báo
        showMessage("Tính năng đăng ký đang được phát triển.", false);
        System.out.println("Chuyển sang màn hình đăng ký (chưa có)");
    }

    // ---------- Các phương thức hỗ trợ giao diện ----------
    private void startLoading() {
        loginButton.setDisable(true);
        loadingIndicator.setVisible(true);
        messageLabel.setVisible(false);
    }

    private void stopLoading() {
        loginButton.setDisable(false);
        loadingIndicator.setVisible(false);
    }

    /**
     * Hiển thị thông báo (thành công hoặc thất bại)
     * @param text nội dung
     * @param isSuccess true nếu là thông báo thành công (màu xanh)
     */
    private void showMessage(String text, boolean isSuccess) {
        messageLabel.setText(text);
        if (isSuccess) {
            messageLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
        messageLabel.setVisible(true);
        // Tự động ẩn sau 3 giây nếu là thông báo lỗi
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

    /**
     * Dọn dẹp khi đóng ứng dụng (ngắt luồng, đóng socket)
     */
    public void shutdown() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        if (!AppConfig.isMockEnabled()) {
            try {
                if (SocketManager.getInstance().isConnected()) {
                    SocketManager.getInstance().disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}