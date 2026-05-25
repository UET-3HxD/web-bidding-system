package com.auction.team3HxD.model.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements NotificationSubject {

  private static NotificationManager instance;

  // Dùng CopyOnWriteArrayList để đảm bảo an toàn (Thread-safe) khi nhiều luồng cùng thêm/xóa client
  private final List<ClientObserver> observers = new CopyOnWriteArrayList<>();

  private NotificationManager() {
  }

  public static synchronized NotificationManager getInstance() {
    if (instance == null) {
      instance = new NotificationManager();
    }
    return instance;
  }

  @Override
  public void addObserver(ClientObserver observer) {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  @Override
  public void removeObserver(ClientObserver observer) {
    observers.remove(observer);
  }

  @Override
  public void notifyAllObservers(String eventType, String payload) {
    String message = eventType + "|" + payload;
    for (ClientObserver observer : observers) {
      observer.onNotify(eventType, message);
    }
  }

  @Override
  public void notifyOthers(String eventType, String payload, int excludeUserId) {
    String message = eventType + "|" + payload;
    for (ClientObserver observer : observers) {
      if (observer.getObserverUserId() != excludeUserId) {
        observer.onNotify(eventType, message);
      }
    }
  }

  @Override
  public void notifySingleUser(String eventType, String payload, int targetUserId) {
    String message = eventType + "|" + payload;
    for (ClientObserver observer : observers) {
      if (observer.getObserverUserId() == targetUserId) {
        observer.onNotify(eventType, message);
        break; // Tìm thấy là dừng luôn
      }
    }
  }

  public void notifySpecificGroup(String eventType, String payload,
      java.util.List<Integer> targetUserIds) {
    String message = eventType + "|" + payload;
    for (ClientObserver observer : observers) {
      if (targetUserIds.contains(observer.getObserverUserId())) {
        observer.onNotify(eventType, message);
      }
    }
  }
}
