package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.AppConfig;
import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private Button registerButton;
    @FXML private Button backButton;
    @FXML private Label messageLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;
    @FXML private Label confirmPasswordError;
    @FXML private Label emailError;

    private Thread listenerThread;
    private volatile boolean running = true;

    @FXML
    public void initialize() {
        // Ẩn tất cả các thành phần phụ trợ ban đầu, kèm setManaged(false) để không chiếm layout
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        passwordStrengthBar.setVisible(false);
        passwordStrengthBar.setManaged(false);
        passwordStrengthLabel.setVisible(false);
        passwordStrengthLabel.setManaged(false);
        confirmPasswordError.setVisible(false);
        confirmPasswordError.setManaged(false);
        emailError.setVisible(false);
        emailError.setManaged(false);

        // Listener real-time cho mật khẩu: cập nhật độ mạnh và kiểm tra xác nhận mật khẩu
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPasswordStrength(newValue);
            checkConfirmPassword();  // Mỗi khi mật khẩu thay đổi, kiểm tra lại xác nhận
        });
        // Listener cho xác nhận mật khẩu
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> checkConfirmPassword());
        // Listener cho email
        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateEmail(newValue));

        if (!AppConfig.isMockEnabled()) {
            startListening();
        }
    }

    private void checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setVisible(false);
            passwordStrengthBar.setManaged(false);
            passwordStrengthLabel.setVisible(false);
            passwordStrengthLabel.setManaged(false);
            return;
        }
        int score = 0;
        if (password.length() >= 6) score++;
        if (password.length() >= 8) score++;
        if (Pattern.matches(".*[A-Z].*", password)) score++;
        if (Pattern.matches(".*[a-z].*", password)) score++;
        if (Pattern.matches(".*\\d.*", password)) score++;
        if (Pattern.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*", password)) score++;

        double progress = Math.min(1.0, score / 6.0);
        passwordStrengthBar.setProgress(progress);
        passwordStrengthBar.setVisible(true);
        passwordStrengthBar.setManaged(true);

        String strengthText;
        String colorStyle;
        if (score < 3) {
            strengthText = "Yếu";
            colorStyle = "#e74c3c";
        } else if (score < 5) {
            strengthText = "Trung bình";
            colorStyle = "#f1c40f";
        } else {
            strengthText = "Mạnh";
            colorStyle = "#2ecc71";
        }
        passwordStrengthLabel.setText("Độ mạnh: " + strengthText);
        passwordStrengthLabel.setStyle("-fx-text-fill: " + colorStyle + ";");
        passwordStrengthLabel.setVisible(true);
        passwordStrengthLabel.setManaged(true);
    }

    private void checkConfirmPassword() {
        String pwd = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        if (confirm.isEmpty()) {
            confirmPasswordError.setVisible(false);
            confirmPasswordError.setManaged(false);
            return;
        }
        if (!pwd.equals(confirm)) {
            confirmPasswordError.setText("Mật khẩu xác nhận không khớp");
            confirmPasswordError.setVisible(true);
            confirmPasswordError.setManaged(true);
        } else {
            confirmPasswordError.setVisible(false);
            confirmPasswordError.setManaged(false);
        }
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) {
            emailError.setVisible(false);
            emailError.setManaged(false);
            return;
        }
        boolean isValid = Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email);
        if (!isValid) {
            emailError.setText("Email không hợp lệ (ví dụ: name@domain.com)");
            emailError.setVisible(true);
            emailError.setManaged(true);
        } else {
            emailError.setVisible(false);
            emailError.setManaged(false);
        }
    }

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
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();

        // Ẩn các thông báo lỗi cũ
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        confirmPasswordError.setVisible(false);
        confirmPasswordError.setManaged(false);
        emailError.setVisible(false);
        emailError.setManaged(false);

        // Kiểm tra dữ liệu đầu vào
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty() || email.isEmpty()) {
            showMessage("Vui lòng điền đầy đủ thông tin.", false);
            return;
        }
        if (!password.equals(confirm)) {
            confirmPasswordError.setText("Mật khẩu xác nhận không khớp");
            confirmPasswordError.setVisible(true);
            confirmPasswordError.setManaged(true);
            return;
        }
        if (password.length() < 6) {
            showMessage("Mật khẩu phải có ít nhất 6 ký tự.", false);
            return;
        }
        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            emailError.setText("Email không hợp lệ.");
            emailError.setVisible(true);
            emailError.setManaged(true);
            return;
        }

        startLoading();

        if (AppConfig.isMockEnabled()) {
            new Thread(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                boolean usernameExists = "admin".equalsIgnoreCase(username);
                boolean emailExists = "admin@example.com".equalsIgnoreCase(email);
                boolean success = !usernameExists && !emailExists;
                Platform.runLater(() -> {
                    stopLoading();
                    if (success) {
                        showMessage("Đăng ký thành công! Chuyển về đăng nhập...", true);
                        new Thread(() -> {
                            try { Thread.sleep(2000); } catch (InterruptedException e) {}
                            Platform.runLater(() -> {
                                SceneSwitcher.getInstance().switchTo("/fxml/login.fxml", registerButton, "Đăng nhập");
                            });
                        }).start();
                    } else if (usernameExists) {
                        showMessage("Tên đăng nhập đã tồn tại.", false);
                    } else if (emailExists) {
                        showMessage("Email đã được sử dụng.", false);
                    } else {
                        showMessage("Đăng ký thất bại, thử lại sau.", false);
                    }
                });
            }).start();
        } else {
            String msg = "REGISTER|" + username + "|" + password + "|" + email;
            SocketManager.getInstance().send(msg);
        }
    }

    private void handleServerResponse(String response) {
        stopLoading();
        if (response.startsWith("REGISTER_OK")) {
            showMessage("Đăng ký thành công! Chuyển về đăng nhập...", true);
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) {}
                Platform.runLater(() -> {
                    SceneSwitcher.getInstance().switchTo("/fxml/login.fxml", registerButton, "Đăng nhập");
                });
            }).start();
        } else if (response.startsWith("REGISTER_ERR_USERNAME_EXISTS")) {
            showMessage("Tên đăng nhập đã tồn tại.", false);
        } else if (response.startsWith("REGISTER_ERR_EMAIL_EXISTS")) {
            showMessage("Email đã được sử dụng.", false);
        } else {
            showMessage("Đăng ký thất bại: " + response, false);
        }
    }

    @FXML
    private void handleBack() {
        SceneSwitcher.getInstance().switchTo("/fxml/login.fxml", backButton, "Đăng nhập");
    }

    private void startLoading() {
        registerButton.setDisable(true);
        loadingIndicator.setVisible(true);
        loadingIndicator.setManaged(true);
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private void stopLoading() {
        registerButton.setDisable(false);
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
    }

    private void showMessage(String text, boolean isSuccess) {
        messageLabel.setText(text);
        messageLabel.setStyle(isSuccess ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
        if (!isSuccess) {
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                Platform.runLater(() -> {
                    messageLabel.setVisible(false);
                    messageLabel.setManaged(false);
                });
            }).start();
        }
    }

    public void shutdown() {
        running = false;
        if (listenerThread != null) listenerThread.interrupt();
    }
}