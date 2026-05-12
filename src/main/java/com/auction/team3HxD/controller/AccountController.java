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

public class AccountController {

    // Các label hiển thị thông tin người dùng
    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblEmailDisplay;
    @FXML private Label lblSidebarName;
    @FXML private Label lblAvatarShort;
    @FXML private Label lblMainAvatarShort;

    @FXML private Button btnAccount;
    @FXML private Button btnAuction;
    @FXML private Button btnBids;
    @FXML private Button btnProducts;
    @FXML private Button btnHelp;
    @FXML private Button btnChangePassword;
    @FXML private Button btnChangeEmail;
    @FXML private Button btnLogout;

    @FXML
    public void initialize() {
        UserSession user = UserSession.getInstance();
        if (user != null) {
            lblFullName.setText(user.getUsername());
            lblEmail.setText(user.getEmail());
            lblEmailDisplay.setText(user.getEmail());
            lblSidebarName.setText(user.getUsername());

            // Avatar: lấy 2 ký tự đầu của tên
            String shortName = user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase();
            lblAvatarShort.setText(shortName);
            lblMainAvatarShort.setText(shortName);
        }
    }

    // ================== ĐIỀU HƯỚNG SIDEBAR ==================
    @FXML
    void handleGoToAccount(ActionEvent event) {
        // Đang ở trang Tài khoản, không làm gì (hoặc reload nếu cần)
        // Có thể hiển thị thông báo nhỏ: "Bạn đang ở trang tài khoản"
    }

    @FXML
    void handleGoToAuction(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/auction_list.fxml",
                (Node) event.getSource(), "Sàn đấu giá");
    }

    @FXML
    void handleGoToMyBids(ActionEvent event) {
        // Tạm thời dùng my_bids.fxml sẽ tạo sau (hiện có thể chưa tồn tại -> sẽ báo lỗi)
        SceneSwitcher.getInstance().switchTo("/fxml/my_bids.fxml",
                (Node) event.getSource(), "Bid đang tham gia");
    }

    @FXML
    void handleGoToMyProducts(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/seller_products.fxml",
                (Node) event.getSource(), "Sản phẩm của bạn");
    }

    @FXML
    void handleGoToHelp(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/help.fxml",
                (Node) event.getSource(), "Trợ giúp");
    }

    // ================== HÀNH ĐỘNG KHÁC (giữ nguyên) ==================
    @FXML
    void handleOpenChangePasswordDialog(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/ChangePasswordView.fxml",
                (Node) event.getSource(), "Đổi mật khẩu");
    }

    @FXML
    void handleOpenChangeEmailDialog(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/ChangeEmailView.fxml",
                (Node) event.getSource(), "Đổi Email");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        ButtonType btnLogout = new ButtonType("Đăng xuất", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Huỷ", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnLogout, btnCancel);

        java.util.Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnLogout) {
            SocketService.getInstance().send("LOGOUT");
            SocketService.getInstance().shutdown();
            UserSession.getInstance().logout();
            SceneSwitcher.getInstance().switchTo("/fxml/login.fxml",
                    (Node) event.getSource(), "Đăng nhập");
        }
    }
}