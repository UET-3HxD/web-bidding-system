package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.NavigationHost;
import com.auction.team3HxD.util.SocketService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;

public class ChangePasswordController implements NavigationConsumer {

    @FXML private PasswordField txtOldPass;
    @FXML private PasswordField txtNewPass;
    @FXML private PasswordField txtConfirmPass;

    private NavigationHost navigationHost;

    @Override
    public void setNavigationHost(NavigationHost host) {
        this.navigationHost = host;
    }

    @FXML
    public void initialize() {
        SocketService.getInstance().setMessageHandler(this::handleServerResponse);
    }

    @FXML
    void handleSavePassword(ActionEvent event) {
        String oldPass = txtOldPass.getText();
        String newPass = txtNewPass.getText();
        String confirmPass = txtConfirmPass.getText();

        if (oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showAlert("Lỗi", "Mật khẩu mới không khớp!");
            return;
        }
        if (newPass.length() < 6) {
            showAlert("Lỗi", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return;
        }

        SocketService.getInstance().send("CHANGE_PASSWORD|" + oldPass + "|" + newPass);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        if (navigationHost != null) {
            navigationHost.navigateTo("/fxml/account_content.fxml");
        }
    }

    private void handleServerResponse(String response) {
        Platform.runLater(() -> {
            if (response.equals("CP_SUCCESS")) {
                showAlert("Thành công", "Đổi mật khẩu thành công!");
                txtOldPass.clear();
                txtNewPass.clear();
                txtConfirmPass.clear();
            } else if (response.startsWith("CP_ERR|")) {
                String errorMsg = response.split("\\|")[1];
                showAlert("Thất bại", "Lỗi: " + errorMsg);
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}