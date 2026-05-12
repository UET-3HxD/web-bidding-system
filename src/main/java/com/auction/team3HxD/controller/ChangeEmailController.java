package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.NavigationHost;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ChangeEmailController implements NavigationConsumer {

    @FXML private TextField txtNewEmail;
    @FXML private PasswordField txtCurrentPass;

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

        SocketService.getInstance().send("CHANGE_EMAIL|" + newEmail + "|" + password);
    }

    @FXML
    void handleCancel(ActionEvent event) {
        if (navigationHost != null) {
            navigationHost.navigateTo("/fxml/account_content.fxml");
        }
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