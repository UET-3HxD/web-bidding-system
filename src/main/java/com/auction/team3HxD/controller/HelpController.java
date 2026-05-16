package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class HelpController {

    @FXML private Label lblSidebarName;
    @FXML private Label lblSidebarAvatar;

    public void initialize() {
        String username = UserSession.getInstance().getUsername();
        if (username != null) {
            lblSidebarName.setText(username);
            String shortName = username.substring(0, Math.min(username.length(), 2)).toUpperCase();
            lblSidebarAvatar.setText(shortName);
        }
    }

    @FXML void handleGoToAccount(ActionEvent e)  { switchTo("/fxml/account.fxml", e); }
    @FXML void handleGoToAuction(ActionEvent e)  { switchTo("/fxml/main_auction.fxml", e); }
    @FXML void handleGoToMyBids(ActionEvent e)   { switchTo("/fxml/my_bids.fxml", e); }
    @FXML void handleGoToProducts(ActionEvent e) { switchTo("/fxml/product_management.fxml", e); }

    private void switchTo(String fxml, ActionEvent e) {
        SceneSwitcher.getInstance().switchTo(fxml, (Node) e.getSource(), "");
    }
}