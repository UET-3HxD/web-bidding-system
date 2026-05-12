package com.auction.team3HxD.controller;

import com.auction.team3HxD.model.User;
import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import java.util.Optional;

public class AccountController {
    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblEmailDisplay;
    @FXML private Label lblSidebarName;
    @FXML private Label lblAvatarShort;
    @FXML private Label lblMainAvatarShort;

    public void initialize() {
        // 1. Lấy dữ liệu người dùng đang đăng nhập từ Session
        UserSession user = UserSession.getInstance();

        if (user != null) {
            // 2. Cập nhật Text cho các Label
            lblFullName.setText(user.getUsername()); // Hoặc user.getFullName() nếu có
            lblEmail.setText(user.getEmail());
            lblSidebarName.setText(user.getUsername());

            // Nếu Captain muốn chuyên nghiệp, hãy lấy 2 chữ cái đầu làm Avatar
            // lblAvatarCircleText.setText(user.getUsername().substring(0, 2).toUpperCase());
        }
    }

    @FXML
    void handleOpenChangePasswordDialog(ActionEvent event) {
        // Ép kiểu event.getSource() thành Node để truyền cho SceneSwitcher
        SceneSwitcher.getInstance().switchTo("/fxml/ChangePasswordView.fxml", (javafx.scene.Node) event.getSource(), "Đổi mật khẩu");
    }

    @FXML
    void handleOpenChangeEmailDialog(ActionEvent event) {
        // Chuyển sang màn hình Đổi Email
        SceneSwitcher.getInstance().switchTo("/fxml/ChangeEmailView.fxml", (javafx.scene.Node) event.getSource(), "Đổi Email");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        // 1. Tạo Popup xác nhận
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?");

        // 2. Tùy chỉnh chữ trên nút bấm cho chuyên nghiệp
        javafx.scene.control.ButtonType btnLogout = new javafx.scene.control.ButtonType("Đăng xuất", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType btnCancel = new javafx.scene.control.ButtonType("Huỷ", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnLogout, btnCancel);

        // 3. Lắng nghe quyết định của người dùng
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnLogout) {
            // 1. Gửi lệnh LOGOUT để Server biết
            SocketService.getInstance().send("LOGOUT");

            // 2. Đóng kết nối ngay lập tức ở phía Client
            SocketService.getInstance().shutdown();

            // 3. Xóa dữ liệu Session để đảm bảo an toàn
            UserSession.getInstance().logout();

            // 4. Chuyển về màn hình Login
            SceneSwitcher.getInstance().switchTo("/fxml/login.fxml", (Node) event.getSource(), "Đăng nhập");
        }
    }
}
