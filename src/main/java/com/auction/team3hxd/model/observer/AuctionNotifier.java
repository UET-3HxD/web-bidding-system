package com.auction.team3hxd.model.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp singleton quản lý danh sách người theo dõi. Cài đặt interface AuctionSubject.
 */
public class AuctionNotifier implements AuctionSubject {
  private static AuctionNotifier instance;
  private static List<AuctionObserver> observers = new ArrayList<>();

  /**
   * Private constructor tránh khởi tạo trực tiếp.
   */
  private AuctionNotifier() {}

  /**
   * Lấy đối tượng.
   */
  public static synchronized AuctionNotifier getInstance() {
    if (instance == null) {
      instance = new AuctionNotifier();
    }
    return instance;
  }

  @Override
  public void registerObserver(AuctionObserver observer) {
    observers.add(observer);
  }

  @Override
  public void removeObserver(AuctionObserver observer) {
    observers.remove(observer);
  }

  @Override
  public void notifyObservers(BidEvent event) {
    for (AuctionObserver observer : observers) {
      observer.onNewBid(event);
    }
  }
}
