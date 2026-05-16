package com.auction.team3HxD.controller;

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

public class ChangeEmailController {

    @FXML private TextField txtNewEmail;
    @FXML private PasswordField txtCurrentPass;
    @FXML private Label lblSidebarName;
    @FXML private Label lblSidebarAvatar;

    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username != null) {
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(username.length(), 2)).toUpperCase();
            lblSidebarAvatar.setText(shortName);
        }
        com.auction.team3HxD.util.SocketService.getInstance().setMessageHandler(this::handleServerResponse);
    }

    @FXML
    void handleSaveEmail(ActionEvent event) {
        String newEmail = txtNewEmail.getText().trim();
        String password = txtCurrentPass.getText();

        if (newEmail.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng email không hợp lệ!");
            return;
        }

        com.auction.team3HxD.util.SocketService.getInstance().send("CHANGE_EMAIL|" + newEmail + "|" + password);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
    }

    @FXML
    void handleBackToAccount(MouseEvent event) {
        SceneSwitcher.getInstance().switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
    }

    // === ĐIỀU HƯỚNG SIDEBAR ===
    @FXML void handleGoToAccount(ActionEvent e)  { switchTo("/fxml/account.fxml", e); }
    @FXML void handleGoToAuction(ActionEvent e)  { switchTo("/fxml/main_auction.fxml", e); }
    @FXML void handleGoToMyBids(ActionEvent e)   { switchTo("/fxml/my_bids.fxml", e); }
    @FXML void handleGoToProducts(ActionEvent e) { switchTo("/fxml/product_management.fxml", e); }
    @FXML void handleGoToHelp(ActionEvent e)    { switchTo("/fxml/help.fxml", e); }

    private void switchTo(String fxml, ActionEvent e) {
        SceneSwitcher.getInstance().switchTo(fxml, (Node) e.getSource(), "");
    }

    private void handleServerResponse(String response) {
        Platform.runLater(() -> {
            if (response.equals("CE_SUCCESS")) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật email thành công!");
                UserSession.getInstance().setEmail(txtNewEmail.getText().trim());
                txtCurrentPass.clear();
            } else if (response.startsWith("CE_ERR|")) {
                String errorType = response.split("\\|")[1];
                String message = "Có lỗi xảy ra!";
                if (errorType.equals("ERR_WRONG_PASSWORD")) message = "Mật khẩu xác thực không chính xác!";
                else if (errorType.equals("ERR_EMAIL_TAKEN")) message = "Email này đã được sử dụng!";
                showAlert(Alert.AlertType.ERROR, "Thất bại", message);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}