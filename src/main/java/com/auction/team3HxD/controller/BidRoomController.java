package com.auction.team3HxD.controller;

import com.auction.team3HxD.dao.AuctionDAO;
import com.auction.team3HxD.util.UserSession;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.text.DecimalFormat;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import com.auction.team3HxD.util.SceneSwitcher;
import javafx.scene.Node;

public class BidRoomController {

  // --- FXML BINDING ---
  @FXML
  private Label lblBreadcrumbName, lblProductName, lblSeller, lblCategory, lblDescription;
  @FXML
  private Label lblStatusBadge, lblTimeLeft, lblMinIncrement, lblHighestBid, lblYourLastBid;
  @FXML
  private ImageView imgProductLarge;
  @FXML
  private VBox vboxNoImage;
  @FXML
  private TextField txtBidInput;
  @FXML
  private Button btnPlaceBid;
  @FXML
  private LineChart<String, Number> lineChartPrice;

  private Timeline countdownTimeline;
  private int remainingSeconds = 0;
  private String currentAuctionId;
  private double currentHighestPrice = 0;
  private double minIncrement = 0;
  private DecimalFormat df = new DecimalFormat("#,###");
  private AuctionDAO auctionDAO = new AuctionDAO();
  private double myLastBid = 0;
  private XYChart.Series<String, Number> priceSeries;

  @FXML
  public void initialize() {
    this.currentAuctionId = String.valueOf(UserSession.getInstance().getSelectedAuctionId());
    int myId = UserSession.getInstance().getId();
    com.auction.team3HxD.util.SocketService.getInstance()
        .setMessageHandler(this::handleServerResponse);
    System.out.println(">>> Đang lấy dữ liệu chi tiết cho Auction ID: " + currentAuctionId);
    com.auction.team3HxD.util.SocketService.getInstance()
        .send("GET_AUCTION_DETAIL|" + currentAuctionId + "|" + myId);
    com.auction.team3HxD.util.SocketService.getInstance()
        .send("GET_AUCTION_BID_HISTORY|" + currentAuctionId);
  }

  private void handleServerResponse(String message) {
    Platform.runLater(() -> {
      String[] parts = message.split("\\|");
      String cmd = parts[0];

      switch (cmd) {
        case "AUCTION_DETAIL_SUCCESS":
          if (parts.length > 1) {
            displayAuctionInfo(parts[1]);
          }
          break;

        case "AUCTION_BID_HISTORY":
          if (parts.length > 1) {
            updateBidChart(parts);
          }
          break;

        case "BID_SUCCESS":
        case "BID_SUCCESS_EXTENDED":
          Platform.runLater(() -> {
            try {
              double newPrice = Double.parseDouble(parts[1]);
              lblHighestBid.setText(df.format(newPrice) + " VNĐ");
              this.currentHighestPrice = newPrice;
              updateYourLastBidDisplay();
              System.out.println(">>> [UI REAL-TIME] Đã cập nhật giá mới: " + newPrice);

              if (priceSeries != null) {
                String now = java.time.LocalTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                XYChart.Data<String, Number> newData = new XYChart.Data<>(now, newPrice);
                priceSeries.getData().add(newData);
                newData.getNode().getStyleClass().add("chart-line-symbol");
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
          break;

        case "BID_ERROR":
          if (parts.length > 1) {
            showAlert("Lỗi đặt giá", parts[1], Alert.AlertType.ERROR);
          }
          break;

        case "BID_UPDATE":
          javafx.application.Platform.runLater(() -> {
            try {
              if (parts.length >= 3) {
                int updatedAuctionId = Integer.parseInt(parts[1]);
                double newHighestPrice = Double.parseDouble(parts[2]);

                if (this.currentAuctionId.equals(String.valueOf(updatedAuctionId))) {
                  this.currentHighestPrice = newHighestPrice;
                  lblHighestBid.setText(df.format(currentHighestPrice) + " VNĐ");
                  updateYourLastBidDisplay();

                  if (priceSeries != null) {
                    String now = java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
                    XYChart.Data<String, Number> newData = new XYChart.Data<>(now, newHighestPrice);
                    priceSeries.getData().add(newData);
                    newData.getNode().getStyleClass().add("chart-line-symbol");
                  }
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
          break;

        case "AUCTION_EXTENDED":
          Platform.runLater(() -> {
            try {
              if (parts.length >= 3) {
                int extendedAuctionId = Integer.parseInt(parts[1]);
                String productName = parts[2];

                if (this.currentAuctionId.equals(String.valueOf(extendedAuctionId))) {
                  txtBidInput.setDisable(false);
                  btnPlaceBid.setDisable(false);
                  lblTimeLeft.getStyleClass().setAll("time-left-normal");

                  Alert alert = new Alert(Alert.AlertType.INFORMATION);
                  alert.setTitle("Thông báo gia hạn");
                  alert.setHeaderText(null);
                  alert.setContentText(
                      "sản phẩm " + productName + " đã được gia hạn thời gian đấu giá!");
                  alert.show();
                  com.auction.team3HxD.util.SocketService.getInstance()
                      .send("GET_AUCTION_DETAIL|" + this.currentAuctionId);
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
          break;

        case "AUCTION_ENDED":
          Platform.runLater(() -> {
            try {
              int endedAuctionId = Integer.parseInt(parts[1]);
              int winnerId = Integer.parseInt(parts[2]);

              if (this.currentAuctionId.equals(String.valueOf(endedAuctionId))) {
                int myUserId = UserSession.getInstance().getId();

                if (myUserId == winnerId) {
                  Alert alert = new Alert(Alert.AlertType.INFORMATION);
                  alert.setTitle("Chúc mừng!");
                  alert.setHeaderText(null);
                  alert.setContentText(
                      "bạn đã thắng sản phẩm này! vào mục \"bid đang tham gia\" để xem sản phẩm vừa thắng.");
                  alert.showAndWait();
                  navigateToMainArea();
                } else {
                  Alert alert = new Alert(Alert.AlertType.INFORMATION);
                  alert.setTitle("Phiên đấu giá kết thúc");
                  alert.setHeaderText(null);
                  alert.setContentText(
                      "phiên đấu giá đã kết thúc. nhấn \"Ok\" để quay trở về khu vực chính.");
                  alert.showAndWait();
                  navigateToMainArea();
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
          break;
      }
    });
  }

  private void updateBidChart(String[] parts) {
    lineChartPrice.getData().clear();
    priceSeries = new XYChart.Series<>();
    priceSeries.setName("Giá");

    if (!parts[1].equals("EMPTY")) {
      for (int i = 1; i < parts.length; i++) {
        String[] point = parts[i].split("#");
        if (point.length == 2) {
          String time = point[0];
          double price = Double.parseDouble(point[1]);
          priceSeries.getData().add(new XYChart.Data<>(time, price));
        }
      }
    }
    lineChartPrice.getData().add(priceSeries);

    for (XYChart.Data<String, Number> data : priceSeries.getData()) {
      data.getNode().getStyleClass().add("chart-line-symbol");
    }
    priceSeries.getNode().getStyleClass().add("chart-series-line");
  }

  private void displayAuctionInfo(String data) {
    String[] info = data.split("#");
      if (info.length < 11) {
          return;
      }

    lblBreadcrumbName.setText(info[1]);
    lblProductName.setText(info[1]);
    lblCategory.setText(info[2]);
    lblSeller.setText(info[3]);

    currentHighestPrice = Double.parseDouble(info[6]);
    minIncrement = Double.parseDouble(info[7]);
    lblMinIncrement.setText(df.format(minIncrement) + " VNĐ");
    lblHighestBid.setText(df.format(currentHighestPrice) + " VNĐ");

    String timeLeftStr = info[8];
    parseAndStartCountdown(timeLeftStr);

    lblDescription.setText(info[9]);

    String imagePath = info[10];
    if (!imagePath.isEmpty()) {
      File file = new File(imagePath);
      if (file.exists()) {
        imgProductLarge.setImage(new Image(file.toURI().toString()));
        vboxNoImage.setVisible(false);
        imgProductLarge.setVisible(true);
      } else {
        System.err.println(">>> Không tìm thấy file ảnh tại: " + imagePath);
        vboxNoImage.setVisible(true);
      }
    }

    double lastBid = auctionDAO.getUserLastBid(Integer.parseInt(currentAuctionId),
        com.auction.team3HxD.util.UserSession.getInstance().getId());
    if (lastBid == 0) {
      lblYourLastBid.setText("---");
    } else {
      lblYourLastBid.setText(df.format(lastBid) + " VNĐ");
    }
    updateYourLastBidDisplay();
  }

  private void parseAndStartCountdown(String timeLeftStr) {
    if (timeLeftStr == null || timeLeftStr.equals("Đã kết thúc")) {
      remainingSeconds = 0;
      lblTimeLeft.setText("Đã kết thúc");
      lblTimeLeft.getStyleClass().setAll("time-left-ended");
      btnPlaceBid.setDisable(true);
      txtBidInput.setDisable(true);
      return;
    }

    String[] timeParts = timeLeftStr.split(":");
    try {
      if (timeParts.length == 3) {
        remainingSeconds = Integer.parseInt(timeParts[0]) * 3600 +
            Integer.parseInt(timeParts[1]) * 60 +
            Integer.parseInt(timeParts[2]);
      } else if (timeParts.length == 2) {
        remainingSeconds = Integer.parseInt(timeParts[0]) * 60 +
            Integer.parseInt(timeParts[1]);
      }

      startCountdown();

    } catch (NumberFormatException e) {
      lblTimeLeft.setText(timeLeftStr);
    }
  }

  private void checkInitialWinningStatus() {
    lblStatusBadge.setText("Sẵn sàng");
    lblStatusBadge.getStyleClass().setAll("badge-ready");

    double lastBid = auctionDAO.getUserLastBid(Integer.parseInt(currentAuctionId),
        com.auction.team3HxD.util.UserSession.getInstance().getId());
    if (lastBid == 0) {
      lblYourLastBid.setText("---");
    } else {
      lblYourLastBid.setText(df.format(lastBid) + " VNĐ");
    }
  }

  private void updateNewBid(String data) {
    String[] info = data.split("#");
    double newPrice = Double.parseDouble(info[1]);

    currentHighestPrice = newPrice;
    Platform.runLater(() -> {
      lblHighestBid.setText(df.format(currentHighestPrice) + " VNĐ");
      updateYourLastBidDisplay();
    });
  }

  @FXML
  void handlePlaceBid(ActionEvent event) {
    String input = txtBidInput.getText().trim();
    if (input.isEmpty()) {
      showAlert("Lỗi", "Vui lòng nhập số tiền!", Alert.AlertType.WARNING);
      return;
    }

    try {
      double bidAmount = Double.parseDouble(input);

      if (bidAmount < currentHighestPrice + minIncrement) {
        showAlert("Giá quá thấp", "Đặt giá không hợp lệ!", Alert.AlertType.WARNING);
        return;
      }

      String userId = "1"; // Demo
      com.auction.team3HxD.util.SocketService.getInstance()
          .send("PLACE_BID|" + currentAuctionId + "|" + userId + "|" + bidAmount);

    } catch (NumberFormatException e) {
      showAlert("Lỗi định dạng", "Vui lòng chỉ nhập số!", Alert.AlertType.ERROR);
    }
  }

  @FXML
  void handleExitRoom(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/main_auction.fxml", (Node) event.getSource(), "Sàn Đấu Giá");
  }

  private void updateYourLastBidDisplay() {
    myLastBid = auctionDAO.getUserLastBid(Integer.parseInt(currentAuctionId),
        com.auction.team3HxD.util.UserSession.getInstance().getId());
    if (myLastBid <= 0) {
      lblYourLastBid.setText("---");
      lblYourLastBid.getStyleClass().setAll("last-bid-empty");
      return;
    }

    lblYourLastBid.setText(df.format(myLastBid) + " VNĐ");

    if (myLastBid >= currentHighestPrice) {
      lblYourLastBid.getStyleClass().setAll("last-bid-winning");
    } else {
      lblYourLastBid.getStyleClass().setAll("last-bid-losing");
    }
  }

  private void handleAuctionEnd() {
    Platform.runLater(() -> {
      txtBidInput.setDisable(true);
      btnPlaceBid.setDisable(true);

      lblTimeLeft.setText("Đang tính kết quả...");
      lblTimeLeft.getStyleClass().setAll("time-left-calculating");

      com.auction.team3HxD.util.SocketService.getInstance()
          .send("CHECK_AUCTION_STATUS|" + this.currentAuctionId);
      System.out.println(">>> [UI] Đã chạm mốc 0s, đang gửi lệnh kiểm tra gia hạn lên Server...");
    });
  }

  private void startCountdown() {
    if (countdownTimeline != null) {
      countdownTimeline.stop();
    }

    countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
      if (remainingSeconds > 0) {
        remainingSeconds--;
        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;
        lblTimeLeft.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
      } else {
        lblTimeLeft.setText("Đã kết thúc");
        lblTimeLeft.getStyleClass().setAll("time-left-ended");
        handleAuctionEnd();
        countdownTimeline.stop();
      }
    }));

    countdownTimeline.setCycleCount(Timeline.INDEFINITE);
    countdownTimeline.play();
  }

  @FXML
  void handleGoToAccount(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/account.fxml", (Node) event.getSource(), "Tài khoản");
  }

  @FXML
  void handleGoToProducts(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/product_management.fxml", (Node) event.getSource(), "Quản lý sản phẩm");
  }

  @FXML
  void handleGoToMyBids(ActionEvent event) {
    SceneSwitcher.getInstance()
        .switchTo("/fxml/my_bids.fxml", (Node) event.getSource(), "Bid đang tham gia");
  }

  @FXML
  void handleGoToHelp(ActionEvent event) {
    SceneSwitcher.getInstance().switchTo("/fxml/help.fxml", (Node) event.getSource(), "Trợ giúp");
  }

  private void refreshBidRoom() {
    try {
      com.auction.team3HxD.util.SceneSwitcher.getInstance().switchTo(
          "/fxml/bid_room.fxml",
          btnPlaceBid,
          "Đấu Giá"
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showExtensionPopupAndRefresh(String productName) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
        javafx.scene.control.Alert.AlertType.INFORMATION);
    alert.setTitle("Thông báo gia hạn");
    alert.setHeaderText(null);
    alert.setContentText("sản phẩm " + productName + " đã được gia hạn thời gian đấu giá!");
    alert.show();
    com.auction.team3HxD.util.SocketService.getInstance()
        .send("GET_AUCTION_DETAIL|" + this.currentAuctionId);
    System.out.println(">>> [UI] Đã yêu cầu Server gửi lại thời gian gia hạn!");
  }

  private void navigateToMainArea() {
    try {
      com.auction.team3HxD.util.SceneSwitcher.getInstance().switchTo(
          "/fxml/main_auction.fxml",
          btnPlaceBid,
          "Sàn Đấu Giá"
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void showAlert(String title, String content, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
}