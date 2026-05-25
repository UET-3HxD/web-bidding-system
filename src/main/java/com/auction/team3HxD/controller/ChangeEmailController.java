package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketService;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class ChangeEmailController {

  @FXML
  private TextField txtNewEmail;
  @FXML
  private PasswordField txtCurrentPass;
  @FXML
  private Label lblSidebarName, lblSidebarAvatar, lblSidebarRole;

  // User
  @FXML
  private Button btnAccount, btnAuction, btnMyBids, btnProducts, btnHelp;
  // Admin
  @FXML
  private Button btnAdminAccount, btnDashboard, btnApprove, btnUsers, btnAdminHelp;

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
    btnAccount.setVisible(!isAdmin);
    btnAccount.setManaged(!isAdmin);
    btnAuction.setVisible(!isAdmin);
    btnAuction.setManaged(!isAdmin);
    btnMyBids.setVisible(!isAdmin);
    btnMyBids.setManaged(!isAdmin);
    btnProducts.setVisible(!isAdmin);
    btnProducts.setManaged(!isAdmin);
    btnHelp.setVisible(!isAdmin);
    btnHelp.setManaged(!isAdmin);

    // Admin
    btnAdminAccount.setVisible(isAdmin);
    btnAdminAccount.setManaged(isAdmin);
    btnDashboard.setVisible(isAdmin);
    btnDashboard.setManaged(isAdmin);
    btnApprove.setVisible(isAdmin);
    btnApprove.setManaged(isAdmin);
    btnUsers.setVisible(isAdmin);
    btnUsers.setManaged(isAdmin);
    btnAdminHelp.setVisible(isAdmin);
    btnAdminHelp.setManaged(isAdmin);

    SocketService.getInstance().setMessageHandler(this::handleServerResponse);
  }

  // User
  @FXML
  void handleGoToAccount(ActionEvent e) {
    switchTo("/fxml/account.fxml", e);
  }

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

  // Admin
  @FXML
  void handleGoToAdminAccount(ActionEvent e) {
    switchTo("/fxml/account.fxml", e);
  }

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
  void handleBackToAccount(MouseEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
  }

  @FXML
  void handleCancel(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
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

  private void handleServerResponse(String response) {
    Platform.runLater(() -> {
      if (response.equals("CE_SUCCESS")) {
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật email thành công!");
        UserSession.getInstance().setEmail(txtNewEmail.getText().trim());
        txtCurrentPass.clear();
      } else if (response.startsWith("CE_ERR|")) {
        String errorType = response.split("\\|")[1];
        String message = "Có lỗi xảy ra!";
          if (errorType.equals("ERR_WRONG_PASSWORD")) {
              message = "Mật khẩu xác thực không chính xác!";
          } else if (errorType.equals("ERR_EMAIL_TAKEN")) {
              message = "Email này đã được sử dụng!";
          }
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