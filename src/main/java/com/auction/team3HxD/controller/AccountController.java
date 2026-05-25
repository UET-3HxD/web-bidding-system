package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;

public class AccountController {

  @FXML
  private Label lblFullName;
  @FXML
  private Label lblEmail;
  @FXML
  private Label lblEmailDisplay;
  @FXML
  private Label lblSidebarName;
  @FXML
  private Label lblAvatarShort;
  @FXML
  private Label lblMainAvatarShort;

  // Nút sidebar
  @FXML
  private Button btnAuction, btnMyBids, btnProducts, btnHelp;        // User
  @FXML
  private Button btnDashboard, btnApprove, btnUsers, btnAdminHelp;   // Admin

  @FXML
  public void initialize() {
    UserSession user = UserSession.getInstance();
    if (user != null) {
      lblFullName.setText(user.getUsername());
      lblEmail.setText(user.getEmail());
      lblEmailDisplay.setText(user.getEmail());
      lblSidebarName.setText(user.getUsername());
      String shortName = user.getUsername().substring(0, Math.min(2, user.getUsername().length()))
          .toUpperCase();
      lblAvatarShort.setText(shortName);
      lblMainAvatarShort.setText(shortName);

      // Ẩn/hiện nút sidebar dựa trên role
      boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
      btnAuction.setVisible(!isAdmin);
      btnAuction.setManaged(!isAdmin);
      btnMyBids.setVisible(!isAdmin);
      btnMyBids.setManaged(!isAdmin);
      btnProducts.setVisible(!isAdmin);
      btnProducts.setManaged(!isAdmin);
      btnHelp.setVisible(!isAdmin);
      btnHelp.setManaged(!isAdmin);

      btnDashboard.setVisible(isAdmin);
      btnDashboard.setManaged(isAdmin);
      btnApprove.setVisible(isAdmin);
      btnApprove.setManaged(isAdmin);
      btnUsers.setVisible(isAdmin);
      btnUsers.setManaged(isAdmin);
      btnAdminHelp.setVisible(isAdmin);
      btnAdminHelp.setManaged(isAdmin);
    }
  }

  // ==================== USER ====================
  @FXML
  void handleGoToAuction(ActionEvent e) {
    switchTo("/fxml/main_auction.fxml", e);
  }

  @FXML
  void handleGoToMyBids(ActionEvent e) {
    switchTo("/fxml/my_bids.fxml", e);
  }

  @FXML
  void handleGoToProducts(ActionEvent e) {
    switchTo("/fxml/product_management.fxml", e);
  }

  @FXML
  void handleGoToHelp(ActionEvent e) {
    switchTo("/fxml/help.fxml", e);
  }

  // ==================== ADMIN ====================
  @FXML
  void handleGoToDashboard(ActionEvent e) {
    switchTo("/fxml/admin_dashboard.fxml", e);
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

  @FXML
  void handleOpenChangePasswordDialog(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/change_password.fxml", (Node) event.getSource(), "Đổi mật khẩu");
  }

  @FXML
  void handleOpenChangeEmailDialog(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/change_email.fxml", (Node) event.getSource(), "Đổi Email");
  }

  @FXML
  void handleLogout(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Xác nhận đăng xuất");
    alert.setHeaderText(null);
    alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?");
    javafx.scene.control.ButtonType btnLogout = new javafx.scene.control.ButtonType("Đăng xuất",
        javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
    javafx.scene.control.ButtonType btnCancel = new javafx.scene.control.ButtonType("Huỷ",
        javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(btnLogout, btnCancel);
    java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == btnLogout) {
      SocketService.getInstance().send("LOGOUT");
      SocketService.getInstance().shutdown();
      UserSession.getInstance().logout();
      SceneSwitcher.getInstance()
          .switchTo("/fxml/login.fxml", (Node) event.getSource(), "Đăng nhập");
    }
  }
}