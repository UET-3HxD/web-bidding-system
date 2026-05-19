package com.auction.team3HxD.model.observer;

public interface ClientObserver {
    void onNotify(String eventType, String payload);
    int getObserverUserId();
}
