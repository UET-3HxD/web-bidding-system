package com.auction.team3HxD.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.auction.team3HxD.util.SceneSwitcher;

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
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        System.out.println("Đăng nhập: " + username + " - " + password);
        // Sau này sẽ gửi request đến server
        SceneSwitcher.getInstance().switchTo("/fxml/auction_list.fxml", loginButton, "Danh sách phiên đấu giá");
    }

    @FXML
    private void handleRegister() {
        System.out.println("Chuyển sang màn hình đăng ký");
        // Sau này sẽ mở màn hình đăng ký
    }
}