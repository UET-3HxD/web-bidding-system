package com.auction.team3hxd.model;

import com.auction.team3hxd.model.enums.AuctionStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho một phiên đấu giá của một sản phẩm cụ thể.
 *
 * @author Huy
 **/
public class Auction extends Entity {
  private Item item;
  private User seller;
  private AuctionStatus status;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private double startPrice;
  private double currentPrice;
  private double bidIncrement;
  private int bidCount;
  private User currentWinner;
  private List<BidTransaction> bidHistory;

  // Getter and Setter
  public double getCurrentPrice() {
    return currentPrice;
  }

  /** Phương thức khởi tạo 1. */
  public Auction(
      User seller,
      Item item,
      double startPrice,
      double bidIncrement,
      String startTime,
      String endTime) {
    this.seller = seller;
    this.item = item;
    this.startPrice = startPrice;
    this.currentPrice = startPrice;
    this.startTime = LocalDateTime.parse(startTime);
    this.endTime = LocalDateTime.parse(endTime);
    this.bidHistory = new ArrayList<>();
    status = AuctionStatus.OPEN;
  }

  /**
   * Phương thức khởi tạo 2.
   *
   * @param id id phiên
   * @param seller người bán
   * @param item sản phẩm
   * @param startPrice giá khởi điểm
   * @param currentPrice giá hiện tại
   * @param bidIncrement bước giá
   * @param status trạng thái
   * @param startTime thời điểm bắt đầu
   * @param endTime thời điểm kết thức
   */
  public Auction(
      int id,
      User seller,
      Item item,
      double startPrice,
      double currentPrice,
      double bidIncrement,
      AuctionStatus status,
      LocalDateTime startTime,
      LocalDateTime endTime) {

    this.setId(id);
    this.seller = seller;
    this.item = item;

    this.startPrice = startPrice;
    this.currentPrice = currentPrice;
    this.bidIncrement = bidIncrement;

    this.status = status;
    this.startTime = startTime;
    this.endTime = endTime;

    this.bidHistory = new ArrayList<>();
  }

  /** Phương thức kết thức đấu giá. */
  public void endAuction() {
    status = AuctionStatus.FINISHED;
  }

  /**
   * Phương thức lấy format của thời gian còn lại.
   *
   * @return chuỗi format thời gian
   */
  public String getTimeLeftFormatted() {
    if (status == AuctionStatus.FINISHED || LocalDateTime.now().isAfter(endTime)) {
      return "Đã kết thúc";
    }

    Duration duration = Duration.between(LocalDateTime.now(), endTime);
    long hours = duration.toHours();
    long minutes = duration.toMinutesPart();
    long seconds = duration.toSecondsPart();

    if (hours > 0) {
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    } else {
      return String.format("%02d:%02d", minutes, seconds);
    }
  }

  //    GETTER
  public User getSeller() {
    return seller;
  }

  public Item getItem() {
    return item;
  }

  public double getStartPrice() {
    return startPrice;
  }

  public double getBidIncrement() {
    return bidIncrement;
  }

  public AuctionStatus getStatus() {
    return status;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public LocalDateTime getEndTime() {
    return endTime;
  }

  public int getBidCount() {
    return bidCount;
  }

  public void setBidCount(int bidCount) {
    this.bidCount = bidCount;
  }
}
