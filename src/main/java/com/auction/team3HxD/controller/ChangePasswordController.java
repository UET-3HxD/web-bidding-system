package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;

public class ChangePasswordController {

    @FXML private PasswordField txtOldPass;
    @FXML private PasswordField txtNewPass;
    @FXML private PasswordField txtConfirmPass;

    @FXML private Label lblSidebarName;
    @FXML private Label lblSidebarAvatar;
    public void initialize() {
        // Đổ dữ liệu vào Sidebar y hệt AccountController
        String username = com.auction.team3HxD.util.UserSession.getInstance().getUsername();
        if (username != null) {
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(username.length(), 2)).toUpperCase();
            lblSidebarAvatar.setText(shortName);
        }
        // Lắng nghe phản hồi từ Server cho trang Đổi Pass
        com.auction.team3HxD.util.SocketService.getInstance().setMessageHandler(this::handleServerResponse);
    }

    @FXML
    void handleSavePassword(ActionEvent event) {
        String oldPass = txtOldPass.getText();
        String newPass = txtNewPass.getText();
        String confirmPass = txtConfirmPass.getText();

        // 1. Validation ngay tại Client
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

        // 2. Gửi lệnh lên Server (chuẩn bị sẵn Loading hoặc vô hiệu hóa nút ở đây nếu cần)
        com.auction.team3HxD.util.SocketService.getInstance().send("CHANGE_PASSWORD|" + oldPass + "|" + newPass);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Quay về trang AccountView
        // Giả sử nút bấm Hủy kích hoạt chuyển trang, truyền event.getSource() làm node
        SceneSwitcher.getInstance().switchTo("/fxml/account_view.fxml", (javafx.scene.Node) event.getSource(), "Tài khoản");
    }

    @FXML
    void handleBackToAccount(javafx.scene.input.MouseEvent event) {
        com.auction.team3HxD.util.SceneSwitcher.getInstance().switchTo(
                "/fxml/account_view.fxml",
                (javafx.scene.Node) event.getSource(),
                "Tài khoản"
        );
    }
    @FXML
    private void handleServerResponse(String response) {
        if (response.equals("CP_SUCCESS")) {
            showAlert("Thành công", "Đổi mật khẩu thành công!");
            // Xóa trắng các ô nhập
            txtOldPass.clear(); txtNewPass.clear(); txtConfirmPass.clear();
        } else if (response.startsWith("CP_ERR|")) {
            // Lấy chuỗi lỗi từ Server gửi về (ví dụ: ERR_WRONG_PASSWORD)
            String errorMsg = response.split("\\|")[1];
            showAlert("Thất bại", "Lỗi: " + errorMsg);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}