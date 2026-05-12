package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.NavigationHost;
import com.auction.team3HxD.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainLayoutController implements NavigationHost {

    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentPane;
    @FXML private Label lblAvatarShort;
    @FXML private Label lblSidebarName;

    @FXML private Button btnAccount;
    @FXML private Button btnAuction;
    @FXML private Button btnBids;
    @FXML private Button btnProducts;
    @FXML private Button btnHelp;

    private Map<String, Button> menuButtons = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        // Key tương ứng với tên file content (bỏ "_content.fxml")
        menuButtons.put("account", btnAccount);
        menuButtons.put("auction_list", btnAuction);
        menuButtons.put("my_bids", btnBids);
        menuButtons.put("seller_products", btnProducts);
        menuButtons.put("help", btnHelp);

        UserSession user = UserSession.getInstance();
        if (user != null) {
            lblSidebarName.setText(user.getUsername());
            String shortName = user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase();
            lblAvatarShort.setText(shortName);
        }

        // Mặc định hiển thị Sàn đấu giá
        navigateTo("/fxml/auction_list_content.fxml");
    }

    @Override
    public void navigateTo(String fxmlPath, Object... data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            Object controller = loader.getController();
            if (controller instanceof NavigationConsumer) {
                ((NavigationConsumer) controller).setNavigationHost(this);
            }
            if (data.length > 0 && controller instanceof DataReceivable) {
                ((DataReceivable) controller).setData(data);
            }

            contentPane.getChildren().setAll(node);
            updateActiveButton(fxmlPath);

        } catch (IOException e) {
            System.err.println("Lỗi load " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace(); // dòng này in chi tiết lỗi ra console
        }
    }

    private void updateActiveButton(String fxmlPath) {
        // Xóa active ở tất cả nút
        menuButtons.values().forEach(btn -> btn.getStyleClass().remove("menu-btn-active"));

        // Lấy key từ đường dẫn, ví dụ: "/fxml/account_content.fxml" -> "account"
        String fileName = fxmlPath.substring(fxmlPath.lastIndexOf("/") + 1); // "account_content.fxml"
        String key = fileName.replace("_content.fxml", "").replace(".fxml", "");
        Button activeBtn = menuButtons.get(key);
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("menu-btn-active");
        }
    }

    @FXML void handleMenuAccount()    { navigateTo("/fxml/account_content.fxml"); }
    @FXML void handleMenuAuction()    { navigateTo("/fxml/auction_list_content.fxml"); }
    @FXML void handleMenuMyBids()     { navigateTo("/fxml/my_bids_content.fxml"); }
    @FXML void handleMenuMyProducts() { navigateTo("/fxml/seller_products_content.fxml"); }
    @FXML void handleMenuHelp()       { navigateTo("/fxml/help_content.fxml"); }
}