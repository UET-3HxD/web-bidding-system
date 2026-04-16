package com.auction.team3HxD.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;

// Dummy class để tạm thời hiển thị dữ liệu (sau này thay bằng model thật)
class AuctionItem {
    private final String productName;
    private final double currentPrice;
    private final String endTime;
    private final String seller;

    public AuctionItem(String productName, double currentPrice, String endTime, String seller) {
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.endTime = endTime;
        this.seller = seller;
    }

    public String getProductName() { return productName; }
    public double getCurrentPrice() { return currentPrice; }
    public String getEndTime() { return endTime; }
    public String getSeller() { return seller; }
}

public class AuctionListController {

    @FXML private TableView<AuctionItem> auctionTableView;
    @FXML private TableColumn<AuctionItem, String> productNameColumn;
    @FXML private TableColumn<AuctionItem, Double> currentPriceColumn;
    @FXML private TableColumn<AuctionItem, String> endTimeColumn;
    @FXML private TableColumn<AuctionItem, String> sellerColumn;

    private ObservableList<AuctionItem> auctionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Gán cột với thuộc tính của AuctionItem
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        sellerColumn.setCellValueFactory(new PropertyValueFactory<>("seller"));

        // Dữ liệu mẫu (sau này thay bằng dữ liệu từ server)
        auctionList.add(new AuctionItem("iPhone 15 Pro Max", 15000000, "20/04/2025 18:00", "Nguyễn Văn A"));
        auctionList.add(new AuctionItem("Tranh sơn dầu", 5000000, "21/04/2025 20:00", "Lê Thị B"));
        auctionList.add(new AuctionItem("Xe máy Honda", 12000000, "22/04/2025 15:00", "Trần Văn C"));

        auctionTableView.setItems(auctionList);
    }

    @FXML
    private void handleRefresh() {
        // Sau này gửi yêu cầu lấy danh sách mới từ server
        System.out.println("Làm mới danh sách");
        // Tạm thời giữ nguyên dữ liệu mẫu
    }

    @FXML
    private void handleViewDetail() {
        AuctionItem selected = auctionTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn một phiên đấu giá để xem chi tiết.");
            return;
        }
        System.out.println("Xem chi tiết: " + selected.getProductName());
        // Sau này chuyển sang màn hình chi tiết (auction_detail.fxml)
        // Có thể truyền ID sản phẩm qua controller
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}