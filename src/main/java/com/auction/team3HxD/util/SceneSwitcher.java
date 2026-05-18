package com.auction.team3HxD.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {

    private static SceneSwitcher instance;

    private SceneSwitcher() {
    }

    public static synchronized SceneSwitcher getInstance() {
        if (instance == null) {
            instance = new SceneSwitcher();
        }
        return instance;
    }

    public void switchTo(String fxmlPath, Node currentNode, String title) {
        try {
            Stage stage = (Stage) currentNode.getScene().getWindow();

            // Lưu trạng thái maximize và kích thước
            boolean wasMaximized = stage.isMaximized();
            double width = stage.getWidth();
            double height = stage.getHeight();

            // Nếu kích thước không hợp lệ, dùng mặc định
            if (width <= 0) width = 1000;
            if (height <= 0) height = 750;

            // Tải Scene mới
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene newScene = new Scene(loader.load());
            stage.setScene(newScene);
            stage.setTitle(title);

            if (wasMaximized) {
                // Quan trọng: set kích thước bình thường trước, rồi mới maximize
                stage.setWidth(width);
                stage.setHeight(height);
                stage.centerOnScreen();
                stage.setMaximized(true);
            } else {
                stage.setWidth(width);
                stage.setHeight(height);
                stage.centerOnScreen();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}