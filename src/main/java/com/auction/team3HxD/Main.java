package com.auction.team3HxD;

import com.auction.team3HxD.util.AppConfig;
import com.auction.team3HxD.util.SocketService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Kết nối socket một lần duy nhất cho cả vòng đời ứng dụng (nếu không dùng mock)
        if (!AppConfig.isMockEnabled()) {
            try {
                SocketService.getInstance().connect(
                        AppConfig.getServerHost(),
                        AppConfig.getServerPort()
                );
                System.out.println("SocketService đã kết nối đến server.");
            } catch (Exception e) {
                e.printStackTrace();
                // Có thể hiển thị cảnh báo cho người dùng ở đây
            }
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Hệ thống đấu giá trực tuyến");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.show();

        // Khi đóng cửa sổ chính, giải phóng socket
        primaryStage.setOnCloseRequest(event -> {
            if (!AppConfig.isMockEnabled()) {
                SocketService.getInstance().shutdown();
                System.out.println("SocketService đã ngắt kết nối.");
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}