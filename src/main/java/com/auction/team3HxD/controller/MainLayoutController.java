// File: src/main/java/com/auction/team3HxD/controller/MainLayoutController.java
package com.auction.team3HxD.controller;

import com.auction.team3HxD.util.NavigationHost;
import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainLayoutController implements NavigationHost {

    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentPane;
    @FXML private Label lblAvatarShort;
    @FXML private Label lblSidebarName;

    // Nút sidebar
    @FXML private Button btnAccount;
    @FXML private Button btnAuction;
    @FXML private Button btnBids;
    @FXML private Button btnProducts;
    @FXML private Button btnHelp;

    // Lưu trạng thái nút hiện tại để dễ quản lý active
    private Map<String, Button> menuButtons = new HashMap<>();

    @FXML
    public void initialize() {
        // Lưu các nút vào map theo tên fxml (key tự đặt)
        menuButtons.put("account", btnAccount);
        menuButtons.put("auction", btnAuction);
        menuButtons.put("my_bids", btnBids);
        menuButtons.put("seller_products", btnProducts);
        menuButtons.put("help", btnHelp);

        // Cập nhật thông tin user
        UserSession user = UserSession.getInstance();
        if (user != null) {
            lblSidebarName.setText(user.getUsername());
            String shortName = user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase();
            lblAvatarShort.setText(shortName);
        }

        // Mặc định mở Sàn đấu giá
        navigateTo("/fxml/auction_list_content.fxml");
    }

    /**
     * Load nội dung vào vùng center.
     * @param fxmlPath đường dẫn file FXML (nội dung)
     * @param data dữ liệu bổ sung (có thể truyền cho controller con qua setter)
     */
    @Override
    public void navigateTo(String fxmlPath, Object... data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            // Nếu controller con cần tham chiếu NavigationHost, ta truyền vào
            Object controller = loader.getController();
            if (controller instanceof NavigationConsumer) {
                ((NavigationConsumer) controller).setNavigationHost(this);
            }
            // Có thể gọi setData nếu có
            if (data.length > 0 && controller instanceof DataReceivable) {
                ((DataReceivable) controller).setData(data);
            }

            contentPane.getChildren().setAll(node);

            // Cập nhật trạng thái active cho nút sidebar
            updateActiveButton(fxmlPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateActiveButton(String fxmlPath) {
        // Reset tất cả nút
        for (Button btn : menuButtons.values()) {
            btn.getStyleClass().remove("menu-btn-active");
        }
        // Xác định key từ fxmlPath (giản lược: tách tên file)
        String key = fxmlPath.replace("/fxml/", "").replace(".fxml", "").replace("_content", "");
        Button activeBtn = menuButtons.get(key);
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("menu-btn-active");
        }
    }

    // ================= CÁC HANDLER CHO SIDEBAR =================
    @FXML void handleMenuAccount() { navigateTo("/fxml/account_content.fxml"); }
    @FXML void handleMenuAuction() { navigateTo("/fxml/auction_list_content.fxml"); }
    @FXML void handleMenuMyBids() { navigateTo("/fxml/my_bids_content.fxml"); }
    @FXML void handleMenuMyProducts() { navigateTo("/fxml/seller_products_content.fxml"); }
    @FXML void handleMenuHelp() { navigateTo("/fxml/help_content.fxml"); }
}