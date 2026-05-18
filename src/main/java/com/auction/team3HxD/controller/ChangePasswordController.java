package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;

public class ChangePasswordController {

    @FXML private PasswordField txtOldPass, txtNewPass, txtConfirmPass;
    @FXML private Label lblSidebarName, lblSidebarAvatar, lblSidebarRole;

    // User
    @FXML private Button btnAccount, btnAuction, btnMyBids, btnProducts, btnHelp;
    // Admin
    @FXML private Button btnAdminAccount, btnDashboard, btnApprove, btnUsers, btnAdminHelp;

    @FXML
    public void initialize() {
        UserSession user = UserSession.getInstance();
        boolean isAdmin = user != null && "ADMIN".equalsIgnoreCase(user.getRole());

        if (user != null) {
            String username = user.getUsername();
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(username.length(), 2)).toUpperCase();
            lblSidebarAvatar.setText(shortName);
            lblSidebarRole.setText(isAdmin ? "Quản trị viên" : "Thành viên");
        }

        // User
        btnAccount.setVisible(!isAdmin); btnAccount.setManaged(!isAdmin);
        btnAuction.setVisible(!isAdmin); btnAuction.setManaged(!isAdmin);
        btnMyBids.setVisible(!isAdmin); btnMyBids.setManaged(!isAdmin);
        btnProducts.setVisible(!isAdmin); btnProducts.setManaged(!isAdmin);
        btnHelp.setVisible(!isAdmin); btnHelp.setManaged(!isAdmin);

        // Admin
        btnAdminAccount.setVisible(isAdmin); btnAdminAccount.setManaged(isAdmin);
        btnDashboard.setVisible(isAdmin); btnDashboard.setManaged(isAdmin);
        btnApprove.setVisible(isAdmin); btnApprove.setManaged(isAdmin);
        btnUsers.setVisible(isAdmin); btnUsers.setManaged(isAdmin);
        btnAdminHelp.setVisible(isAdmin); btnAdminHelp.setManaged(isAdmin);

        com.auction.team3HxD.util.SocketService.getInstance().setMessageHandler(this::handleServerResponse);
    }

    // User
    @FXML void handleGoToAccount(ActionEvent e)  { switchTo("/fxml/account.fxml", e); }
    @FXML void handleGoToAuction(ActionEvent e)  { switchTo("/fxml/main_auction.fxml", e); }
    @FXML void handleGoToMyBids(ActionEvent e)   { switchTo("/fxml/my_bids.fxml", e); }
    @FXML void handleGoToProducts(ActionEvent e) { switchTo("/fxml/product_management.fxml", e); }
    @FXML void handleGoToHelp(ActionEvent e)    { switchTo("/fxml/help.fxml", e); }

    // Admin
    @FXML void handleGoToAdminAccount(ActionEvent e) { switchTo("/fxml/account.fxml", e); }
    @FXML void handleGoToDashboard(ActionEvent e)    { switchTo("/fxml/admin_dashboard.fxml", e); }
    @FXML void handleGoToApprove(ActionEvent e)      { switchTo("/fxml/admin_approve_products.fxml", e); }
    @FXML void handleGoToUsers(ActionEvent e)        { switchTo("/fxml/admin_manage_users.fxml", e); }
    @FXML void handleGoToAdminHelp(ActionEvent e) { switchTo("/fxml/help.fxml", e); }

    private void switchTo(String fxml, ActionEvent e) {
        SceneSwitcher.getInstance().switchTo(fxml, (Node) e.getSource(), "");
    }

    @FXML void handleBackToAccount(MouseEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
    }

    @FXML void handleCancel(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
    }

    @FXML void handleSavePassword(ActionEvent event) {
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
        com.auction.team3HxD.util.SocketService.getInstance().send("CHANGE_PASSWORD|" + oldPass + "|" + newPass);
    }

    private void handleServerResponse(String response) {
        Platform.runLater(() -> {
            if (response.equals("CP_SUCCESS")) {
                showAlert("Thành công", "Đổi mật khẩu thành công!");
                txtOldPass.clear(); txtNewPass.clear(); txtConfirmPass.clear();
            } else if (response.startsWith("CP_ERR|")) {
                showAlert("Thất bại", "Lỗi: " + response.split("\\|")[1]);
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