package com.auction.team3hxd.model.observer;

/**
 * Interface định nghĩa các hành vi của đối tượng được quan sát.
 *
 * @author Duc
 */
public interface NotificationSubject {

    /**
     * Thêm người quan sát vào danh sách của phiên đấu giá.
     *
     * @param observer đối tượng quan sát
     */
    void addObserver(ClientObserver observer);

    /**
     * Xóa người quan sát khỏi danh sách của phiên đấu giá.
     *
     * @param observer đối tượng quan sát
     */
    void removeObserver(ClientObserver observer);

    /**
     * Phương thức gửi thông báo cho tất cả đoói tương quan sát.
     *
     * @param eventType loại sự kiện
     * @param payload   thông tin chi tiết sự kiện
     */
    void notifyAllObservers(String eventType, String payload);

    /**
     * Phương thức gửi thông báo cho tất cả người quan sát (trừ người gây ra sự kiện).
     *
     * @param eventType     loại sự kiện
     * @param payload       thông tin chi tiết sự kiện
     * @param excludeUserId id người gây ra sự kiện
     */
    void notifyOthers(String eventType, String payload, int excludeUserId);

    /**
     * Phương thức gửi riêng cho một đối tượng quan sát.
     *
     * @param eventType    loại sự kiện
     * @param payload      thông tin chi tiết sự kiện
     * @param targetUserId id đối tượng quan sát
     */
    void notifySingleUser(String eventType, String payload, int targetUserId);
}
