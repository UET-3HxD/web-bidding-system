package com.auction.team3HxD.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneSwitcher {

    private static SceneSwitcher instance;

    private SceneSwitcher() {}

    public static synchronized SceneSwitcher getInstance() {
        if (instance == null) {
            instance = new SceneSwitcher();
        }
        return instance;
    }

    public void switchTo(String fxmlPath, Node currentNode, String title) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(fxmlPath));

            Parent root = loader.load();

            // Lấy Stage hiện tại
            Stage stage =
                    (Stage) currentNode.getScene().getWindow();

            // Lấy Scene hiện tại
            Scene currentScene = stage.getScene();

            // Chỉ thay root -> mượt hơn, không nháy fullscreen
            currentScene.setRoot(root);

            stage.setTitle(title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}