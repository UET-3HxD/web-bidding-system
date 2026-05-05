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
        messageLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        passwordStrengthBar.setVisible(false);
        confirmPasswordError.setVisible(false);
        emailError.setVisible(false);

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> checkPasswordStrength(newVal));
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> checkConfirmPassword());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateEmail(newVal));

        if (!AppConfig.isMockEnabled()) {
            startListening();
        }
    }

    private void checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setVisible(false);
            passwordStrengthLabel.setText("");
            return;
        }
        passwordStrengthBar.setVisible(true);
        int score = 0;
        if (password.length() >= 6) score++;
        if (password.length() >= 8) score++;
        if (Pattern.matches(".*[A-Z].*", password)) score++;
        if (Pattern.matches(".*[a-z].*", password)) score++;
        if (Pattern.matches(".*\\d.*", password)) score++;
        if (Pattern.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*", password)) score++;

        double progress = Math.min(1.0, score / 6.0);
        passwordStrengthBar.setProgress(progress);

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
    }

    private void checkConfirmPassword() {
        String pwd = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        if (confirm.isEmpty()) {
            confirmPasswordError.setVisible(false);
            return;
        }
        if (!pwd.equals(confirm)) {
            confirmPasswordError.setText("Mật khẩu xác nhận không khớp");
            confirmPasswordError.setVisible(true);
        } else {
            confirmPasswordError.setVisible(false);
        }
    }

    private void validateEmail(String email) {
        if (email.isEmpty()) {
            emailError.setVisible(false);
            return;
        }
        boolean isValid = Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email);
        if (!isValid) {
            emailError.setText("Email không hợp lệ (ví dụ: name@domain.com)");
            emailError.setVisible(true);
        } else {
            emailError.setVisible(false);
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

        messageLabel.setVisible(false);
        confirmPasswordError.setVisible(false);
        emailError.setVisible(false);

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty() || email.isEmpty()) {
            showMessage("Vui lòng điền đầy đủ thông tin.", false);
            return;
        }
        if (!password.equals(confirm)) {
            confirmPasswordError.setText("Mật khẩu xác nhận không khớp");
            confirmPasswordError.setVisible(true);
            return;
        }
        if (password.length() < 6) {
            showMessage("Mật khẩu phải có ít nhất 6 ký tự.", false);
            return;
        }
        if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email)) {
            emailError.setText("Email không hợp lệ.");
            emailError.setVisible(true);
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
                            Platform.runLater(() -> SceneSwitcher.getInstance().switchTo("/fxml/login.fxml", registerButton, "Đăng nhập"));
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
                Platform.runLater(() -> SceneSwitcher.getInstance().switchTo("/fxml/login.fxml", registerButton, "Đăng nhập"));
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
        messageLabel.setVisible(false);
    }

    private void stopLoading() {
        registerButton.setDisable(false);
        loadingIndicator.setVisible(false);
    }

    private void showMessage(String text, boolean isSuccess) {
        messageLabel.setText(text);
        messageLabel.setStyle(isSuccess ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
        if (!isSuccess) {
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                Platform.runLater(() -> messageLabel.setVisible(false));
            }).start();
        }
    }

    public void shutdown() {
        running = false;
        if (listenerThread != null) listenerThread.interrupt();
    }
}