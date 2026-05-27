package com.auction.team3hxd.exception;

/**
 * Ném ra exception này khi cố thực hiện thao tác trong khi Auction đã đóng
 */
public class AuctionClosedException extends Exception {

    public AuctionClosedException(String message) {
        super(message);
    }
}
