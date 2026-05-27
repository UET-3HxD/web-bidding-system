package com.auction.team3hxd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage globalStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        globalStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("Hệ thống đấu giá trực tuyến");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(false);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}