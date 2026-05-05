package com.auction.team3HxD.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Lớp tiện ích dùng để chuyển đổi giữa các màn hình trong ứng dụng.
 * Áp dụng Singleton để đảm bảo chỉ có một bộ điều phối duy nhất.
 */
public class SceneSwitcher {

    // 1. Biến tĩnh lưu thể hiện duy nhất của lớp
    private static SceneSwitcher instance;

    // 2. Hàm khởi tạo private -> không cho phép tạo đối tượng từ bên ngoài
    private SceneSwitcher() {
    }

    /**
     * Lấy thể hiện duy nhất của SceneSwitcher (khởi tạo muộn, an toàn đa luồng).
     * @return thể hiện duy nhất của SceneSwitcher
     */
    public static synchronized SceneSwitcher getInstance() {
        if (instance == null) {
            instance = new SceneSwitcher();
        }
        return instance;
    }

    /**
     * Chuyển cửa sổ hiện tại sang màn hình mới được định nghĩa bởi file FXML.
     *
     * @param fxmlPath   Đường dẫn đến file FXML (ví dụ: "/fxml/auction_list.fxml")
     * @param currentNode Một node bất kỳ trong màn hình hiện tại (thường là nút bấm) để lấy Stage
     * @param title       Tiêu đề cho cửa sổ mới
     */
    public void switchTo(String fxmlPath, Node currentNode, String title) {
        try {
            // Tải file FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Lấy Stage từ node hiện tại
            Stage stage = (Stage) currentNode.getScene().getWindow();

            // Thiết lập Scene mới
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen(); // Căn giữa màn hình
        } catch (Exception e) {
            e.printStackTrace();
            // Có thể thêm thông báo lỗi cho người dùng nếu cần
        }
    }
}