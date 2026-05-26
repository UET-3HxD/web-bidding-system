package com.auction.team3hxd.model.observer;

/**
 * Interface cho đối tượng được quan sát.
 */
public interface AuctionSubject {
  /**
   * Đăng ký theo dõi.
   *
   * @param observer người theo dõi
   */
  void registerObserver(AuctionObserver observer);

  /**
   * Xóa người theo dõi.
   */
  void removeObserver(AuctionObserver observer);

  /**
   * Thông báo.
   */
  void notifyObservers(BidEvent event);
}
