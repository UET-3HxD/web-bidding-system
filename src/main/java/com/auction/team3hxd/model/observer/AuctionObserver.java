package com.auction.team3hxd.model.observer;

/**
 * Interface cho người quan sát.
 */
public interface AuctionObserver {
  /**
   * Gọi khi có sự kiện đặt giá xảy ra.
   *
   * @param event sự kiện
   */
  void onNewBid(BidEvent event);
}
