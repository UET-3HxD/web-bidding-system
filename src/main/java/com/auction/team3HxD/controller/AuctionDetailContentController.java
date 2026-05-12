package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.NavigationHost;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class AuctionDetailContentController implements NavigationConsumer, DataReceivable {

    @FXML private Label lblItemName;
    @FXML private Label lblTitle;
    @FXML private Label lblStatus;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblTimeLeft;
    @FXML private TextField txtBidAmount;

    private NavigationHost navigationHost;
    private String itemName;

    @Override
    public void setNavigationHost(NavigationHost host) {
        this.navigationHost = host;
    }

    @Override
    public void setData(Object... data) {
        if (data.length > 0 && data[0] instanceof String) {
            this.itemName = (String) data[0];
            if (lblItemName != null) {
                lblItemName.setText(itemName);
                lblTitle.setText(itemName);
            }
        }
    }

    @FXML
    public void initialize() {
        if (itemName != null) {
            lblItemName.setText(itemName);
            lblTitle.setText(itemName);
        } else {
            lblItemName.setText("Sản phẩm mẫu");
            lblTitle.setText("Sản phẩm mẫu");
        }
        lblCurrentPrice.setText("12.500.000 ₫");
        lblTimeLeft.setText("⏳ 2 giờ 15 phút");
        lblStatus.setText("Đang đấu giá");
        lblStatus.getStyleClass().add("status-badge-active");
    }

    @FXML
    void handlePlaceBid(ActionEvent event) {
        String bid = txtBidAmount.getText().trim();
        if (bid.isEmpty()) {
            System.out.println("Vui lòng nhập giá.");
            return;
        }
        System.out.println("Đặt giá: " + bid);
    }

    @FXML
    void handleGoBack(ActionEvent event) {
        if (navigationHost != null) {
            navigationHost.navigateTo("/fxml/auction_list_content.fxml");
        }
    }

    @FXML void handleGoHome() { handleGoBack(null); }
    @FXML void handleGoAuction() { handleGoBack(null); }
}