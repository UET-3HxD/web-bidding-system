package com.auction.team3hxd.model.observer;

/**
 * Interface đại diện cho một người quan sát trong hệ thống.
 *
 * @author Duc
 */
public interface ClientObserver {

    /**
     * Phương thức đợc gọi khi có sự kiện đấu giá mới.
     *
     * @param eventType loại sự kiện xảy ra
     * @param payload   Dữ liệu chi tiết của sự kiện
     */
    void onNotify(String eventType, String payload);

    /**
     * Lấy id của người quan sát.
     *
     * @return id người quan sát dạng int
     */
    int getObserverUserId();
}
