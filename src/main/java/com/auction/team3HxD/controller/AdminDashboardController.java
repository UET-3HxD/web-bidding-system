package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class AdminDashboardController {

    @FXML private Label lblSidebarName;
    @FXML private Label lblSidebarAvatar;
    @FXML private Label lblPendingCount;
    @FXML private Label lblLiveCount;
    @FXML private Label lblUserCount;
    @FXML private Label lblServerStatus;
    @FXML private Label lblUptime;

    @FXML
    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username != null) {
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(username.length(), 2)).toUpperCase();
            lblSidebarAvatar.setText(shortName);
        }
        // TODO: Lấy dữ liệu thống kê từ server
        lblPendingCount.setText("...");
        lblLiveCount.setText("...");
        lblUserCount.setText("...");
        lblServerStatus.setText("🟢 Đang hoạt động");
        lblUptime.setText("...");
    }

    // Điều hướng sidebar
    @FXML void handleGoToAccount(ActionEvent e) { switchTo("/fxml/account.fxml", e); }
    @FXML void handleGoToApprove(ActionEvent e)  { switchTo("/fxml/admin_approve_products.fxml", e); }
    @FXML void handleGoToUsers(ActionEvent e)    { switchTo("/fxml/admin_manage_users.fxml", e); }
    @FXML void handleGoToAdminHelp(ActionEvent e)    { switchTo("/fxml/help.fxml", e); }

    private void switchTo(String fxml, ActionEvent e) {
        SceneSwitcher.getInstance().switchTo(fxml, (Node) e.getSource(), "");
    }
}