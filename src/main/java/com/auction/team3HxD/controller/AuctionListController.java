package com.auction.team3HxD.controller;

import com.auction.team3HxD.model.AuctionInfo;
import com.auction.team3HxD.util.AppConfig;
import com.auction.team3HxD.util.SceneSwitcher;
import com.auction.team3HxD.util.SocketManager;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;

public class AuctionListController {

    @FXML private TableView<AuctionInfo> auctionTableView;
    @FXML private TableColumn<AuctionInfo, String> productNameColumn;
    @FXML private TableColumn<AuctionInfo, Double> currentPriceColumn;
    @FXML private TableColumn<AuctionInfo, String> endTimeColumn;
    @FXML private TableColumn<AuctionInfo, String> sellerColumn;
    @FXML private Button refreshButton;
    @FXML private Button detailButton;
    @FXML private Label messageLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private ObservableList<AuctionInfo> auctionList = FXCollections.observableArrayList();
    private Thread listenerThread;
    private volatile boolean running = true;

    @FXML
    public void initialize() {
        // Dùng lambda để gán dữ liệu
        productNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProductName()));
        currentPriceColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCurrentPrice()));
        endTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndTime()));
        sellerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSeller()));

        auctionTableView.setItems(auctionList);
        messageLabel.setVisible(false);
        loadingIndicator.setVisible(false);

        if (AppConfig.isMockEnabled()) {
            loadMockData();
        } else {
            startListening();
            loadAuctionsFromServer();
        }
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            while (running) {
                try {
                    String response = SocketManager.getInstance().receive();
                    if (response != null) {
                        final String res = response;
                        Platform.runLater(() -> handleServerResponse(res));
                    }
                } catch (IOException e) {
                    if (running) {
                        Platform.runLater(() -> showMessage("Mất kết nối đến server.", false));
                        break;
                    }
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void loadMockData() {
        auctionList.clear();
        auctionList.add(new AuctionInfo(1, "iPhone 15 Pro Max", 15000000, "20/04/2025 18:00", "Nguyễn Văn A"));
        auctionList.add(new AuctionInfo(2, "Tranh sơn dầu", 5000000, "21/04/2025 20:00", "Lê Thị B"));
        auctionList.add(new AuctionInfo(3, "Xe máy Honda", 12000000, "22/04/2025 15:00", "Trần Văn C"));
        System.out.println("Mock data loaded, size: " + auctionList.size());
    }

    private void loadAuctionsFromServer() {
        if (!SocketManager.getInstance().isConnected()) {
            showMessage("Chưa kết nối đến server. Hãy thử lại sau.", false);
            return;
        }
        auctionList.clear();
        SocketManager.getInstance().send("GET_AUCTIONS");
    }

    private void handleServerResponse(String response) {
        if (response.startsWith("AUCTION|")) {
            String[] parts = response.split("\\|");
            if (parts.length >= 6) {
                try {
                    int id = Integer.parseInt(parts[1]);
                    String productName = parts[2];
                    double currentPrice = Double.parseDouble(parts[3]);
                    String endTime = parts[4];
                    String seller = parts[5];
                    auctionList.add(new AuctionInfo(id, productName, currentPrice, endTime, seller));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } else if (response.equals("END_AUCTIONS")) {
            System.out.println("Hoàn tất nhận danh sách.");
        } else if (response.startsWith("ERR_")) {
            showMessage("Lỗi server: " + response, false);
        }
    }

    @FXML
    private void handleRefresh() {
        if (AppConfig.isMockEnabled()) {
            loadMockData();
        } else {
            loadAuctionsFromServer();
        }
    }

    @FXML
    private void handleViewDetail() {
        AuctionInfo selected = auctionTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Vui lòng chọn một phiên đấu giá.", false);
            return;
        }
        UserSession.getInstance().setSelectedAuctionId(selected.getId());
        SceneSwitcher.getInstance().switchTo("/fxml/auction_detail.fxml", detailButton, "Chi tiết phiên đấu giá");
    }

    private void showMessage(String text, boolean isSuccess) {
        messageLabel.setText(text);
        messageLabel.setStyle(isSuccess ? "-fx-text-fill: #2ecc71;" : "-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
        if (!isSuccess) {
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException e) {}
                Platform.runLater(() -> messageLabel.setVisible(false));
            }).start();
        }
    }

    public void shutdown() {
        running = false;
        if (listenerThread != null) listenerThread.interrupt();
    }
}