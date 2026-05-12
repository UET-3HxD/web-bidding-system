package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SocketManager;
import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

public class ChangeEmailController {

    @FXML private TextField txtNewEmail;
    @FXML private PasswordField txtCurrentPass;
    @FXML private Label lblSidebarName;
    @FXML private Label lblSidebarAvatar;

    public void initialize() {
        // Đổ dữ liệu Sidebar
        String username = UserSession.getInstance().getUsername();
        if (username != null) {
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(username.length(), 2)).toUpperCase();
            lblSidebarAvatar.setText(shortName);
        }
        // Đăng ký nhận phản hồi từ Server dành riêng cho chức năng Email
        com.auction.team3HxD.util.SocketService.getInstance().setMessageHandler(this::handleServerResponse);
    }

    /**
     * Xử lý khi bấm nút "Lưu thay đổi"
     */
    @FXML
    void handleSaveEmail(ActionEvent event) {
        String newEmail = txtNewEmail.getText().trim();
        String password = txtCurrentPass.getText();

        // 1. Kiểm tra dữ liệu đầu vào (Validation)
        if (newEmail.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Kiểm tra định dạng Email cơ bản bằng Regex
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng email không hợp lệ!");
            return;
        }

        // 2. Gửi yêu cầu lên Server
        // Cấu trúc: CHANGE_EMAIL|email_mới|mật_khẩu_xác_thực
        com.auction.team3HxD.util.SocketService.getInstance().send("CHANGE_EMAIL|" + newEmail + "|" + password);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Quay lại màn hình quản lý tài khoản chính
        SceneSwitcher.getInstance().switchTo("/fxml/account_view.fxml", (Node) event.getSource(), "Tài khoản");
    }

    @FXML
    void handleBackToAccount(MouseEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/account_view.fxml", (Node) event.getSource(), "Tài khoản");
    }
    private void handleServerResponse(String response) {
        if (response.equals("CE_SUCCESS")) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật địa chỉ email thành công!");

            // Cập nhật lại thông tin trong Session ở phía Client để hiển thị đúng ở màn hình sau
            UserSession.getInstance().setEmail(txtNewEmail.getText().trim());

            // Xóa trắng ô mật khẩu để bảo mật
            txtCurrentPass.clear();
        } else if (response.startsWith("CE_ERR|")) {
            String errorType = response.split("\\|")[1];
            String message = "Có lỗi xảy ra!";

            // Chuyển mã lỗi khô khan từ Server thành thông báo dễ hiểu
            if (errorType.equals("ERR_WRONG_PASSWORD")) message = "Mật khẩu xác thực không chính xác!";
            else if (errorType.equals("ERR_EMAIL_TAKEN")) message = "Email này đã được sử dụng bởi người dùng khác!";

            showAlert(Alert.AlertType.ERROR, "Thất bại", message);
        }
    }

    /**
     * Hàm hỗ trợ hiển thị thông báo nhanh
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}