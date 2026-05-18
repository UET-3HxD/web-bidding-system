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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) currentNode.getScene().getWindow();
            Scene currentScene = stage.getScene();
            currentScene.setRoot(root);
            stage.setTitle(title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void switchTo(String fxmlPath, Stage targetStage, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene currentScene = targetStage.getScene();
            currentScene.setRoot(root);
            targetStage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}