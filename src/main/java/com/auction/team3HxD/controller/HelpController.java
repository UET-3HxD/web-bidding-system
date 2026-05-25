package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HelpController {

  @FXML
  private Label lblSidebarName;
  @FXML
  private Label lblSidebarAvatar;
  @FXML
  private Label lblSidebarRole;

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
      lblSidebarName.setText(user.getUsername());
      String shortName = user.getUsername().substring(0, Math.min(2, user.getUsername().length()))
          .toUpperCase();
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

  private void switchTo(String fxml, ActionEvent e) {
    SceneSwitcher.getInstance().switchTo(fxml, (Node) e.getSource(), "");
  }
}