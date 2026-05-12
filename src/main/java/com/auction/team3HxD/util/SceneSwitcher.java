package com.auction.team3HxD.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneSwitcher {

    private static SceneSwitcher instance;
    private Map<String, Stage> openStages = new HashMap<>(); // Quản lý các cửa sổ đã mở

    private SceneSwitcher() {}

    public static SceneSwitcher getInstance() {
        if (instance == null) {
            instance = new SceneSwitcher();
        }
        return instance;
    }

    /**
     * Phương thức cũ: thay đổi Scene trên Stage hiện tại.
     * Vẫn giữ nguyên để các màn hình khác (login, register) không bị ảnh hưởng.
     */
    public void switchTo(String fxmlPath, Node currentUIElement, String title) {
        try {
            Stage stage = (Stage) currentUIElement.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Phương thức mới: mở FXML trong một cửa sổ (Stage) độc lập.
     * @param fxmlPath đường dẫn file FXML
     * @param title tiêu đề cửa sổ
     * @return Stage mới được tạo, có thể dùng để điều khiển sau
     */
    public Stage switchToNewWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));
            newStage.show();
            openStages.put(fxmlPath, newStage); // lưu lại nếu cần quản lý
            return newStage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Đóng một cửa sổ đã mở bằng switchToNewWindow (nếu biết fxmlPath hoặc Stage).
     */
    public void closeWindow(String fxmlPath) {
        Stage stage = openStages.get(fxmlPath);
        if (stage != null && stage.isShowing()) {
            stage.close();
            openStages.remove(fxmlPath);
        }
    }

    // Có thể thêm các tiện ích khác như setResizable,...
}