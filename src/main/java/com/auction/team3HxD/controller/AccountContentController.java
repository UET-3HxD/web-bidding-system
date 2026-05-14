package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.NavigationHost;
import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar;

public class AccountContentController implements NavigationConsumer {

    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblEmailDisplay;
    @FXML private Label lblMainAvatarShort;

    @FXML private Button btnChangePassword;
    @FXML private Button btnChangeEmail;
    @FXML private Button btnLogout;

    private NavigationHost navigationHost;

    @Override
    public void setNavigationHost(NavigationHost host) {
        this.navigationHost = host;
        System.out.println("AccountContentController nhận NavigationHost: " + host);
    }
    @FXML
    public void initialize() {
        UserSession user = UserSession.getInstance();
        if (user != null) {
            lblFullName.setText(user.getUsername());
            lblEmail.setText(user.getEmail());
            lblEmailDisplay.setText(user.getEmail());
            String shortName = user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase();
            lblMainAvatarShort.setText(shortName);
        }
    }

    @FXML
    void handleOpenChangePasswordDialog(ActionEvent event) {
        System.out.println("Đổi mật khẩu được bấm. NavigationHost: " + navigationHost);
        if (navigationHost != null) {
            navigationHost.navigateTo("/fxml/change_password_content.fxml");
        } else {
            System.err.println("NavigationHost is NULL!");
        }
    }

    @FXML
    void handleOpenChangeEmailDialog(ActionEvent event) {
        if (navigationHost != null) {
            navigationHost.navigateTo("/fxml/change_email_content.fxml");
        }
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
            SceneSwitcher.getInstance().switchTo("/fxml/login.fxml", (Node) event.getSource(), "Đăng nhập");
        }
    }
}