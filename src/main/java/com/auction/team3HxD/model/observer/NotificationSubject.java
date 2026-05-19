package com.auction.team3HxD.model.observer;

public interface NotificationSubject {
    void addObserver(ClientObserver observer);
    void removeObserver(ClientObserver observer);

    // Gửi cho TẤT CẢ mọi người
    void notifyAllObservers(String eventType, String payload);

    // Gửi cho tất cả TRỪ người gây ra sự kiện (dùng khi Bid)
    void notifyOthers(String eventType, String payload, int excludeUserId);

    // (Nâng cao) Gửi riêng cho 1 người (dùng khi báo chiến thắng)
    void notifySingleUser(String eventType, String payload, int targetUserId);
}
