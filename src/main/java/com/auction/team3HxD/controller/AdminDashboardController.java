package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class AdminDashboardController {

  @FXML
  private Label lblSidebarName;
  @FXML
  private Label lblSidebarAvatar;
  @FXML
  private Label lblPendingCount;
  @FXML
  private Label lblLiveCount;
  @FXML
  private Label lblUserCount;
  @FXML
  private Label lblServerStatus;
  @FXML
  private Label lblUptime;

  @FXML
  public void initialize() {
    String username = UserSession.getInstance().getUsername();
    if (username != null) {
      lblSidebarName.setText(username);
      String shortName = username.substring(0, Math.min(username.length(), 2)).toUpperCase();
      lblSidebarAvatar.setText(shortName);
    }

    // 1. Đăng ký nhận tin nhắn từ Server
    SocketService.getInstance().setMessageHandler(this::handleServerResponse);

    // 2. Gửi lệnh yêu cầu lấy dữ liệu thống kê
    SocketService.getInstance().send("GET_ADMIN_DASHBOARD");
  }

  // Hàm xử lý dữ liệu Server trả về
  private void handleServerResponse(String message) {
    Platform.runLater(() -> {
      String[] parts = message.split("\\|");
      String cmd = parts[0];

      if (cmd.equals("ADMIN_DASHBOARD_SUCCESS")) {
        // Định dạng nhận: ADMIN_DASHBOARD_SUCCESS|pendingCount|liveCount|userCount|uptime
        if (parts.length >= 5) {
          lblPendingCount.setText(parts[1]);
          lblLiveCount.setText(parts[2]);
          lblUserCount.setText(parts[3]);
          lblUptime.setText(parts[4]);
          lblServerStatus.setText("🟢 Đang hoạt động");
        }
      } else if (message.startsWith("PRODUCT_SUBMITTED")) {
        SocketService.getInstance().send("GET_ADMIN_DASHBOARD");
      }
    });
  }

  // --- Các hàm điều hướng sidebar giữ nguyên ---
  @FXML
  void handleGoToAccount(ActionEvent e) {
    switchTo("/fxml/account.fxml", e);
  }

  @FXML
  void handleGoToApprove(ActionEvent e) {
    switchTo("/fxml/admin_approve_products.fxml", e);
  }

  @FXML
  void handleGoToUsers(ActionEvent e) {
    switchTo("/fxml/admin_manage_users.fxml", e);
  }

  @FXML
  void handleGoToAdminHelp(ActionEvent e) {
    switchTo("/fxml/help.fxml", e);
  }

  private void switchTo(String fxml, ActionEvent e) {
    SceneSwitcher.getInstance().switchTo(fxml, (Node) e.getSource(), "");
  }
}