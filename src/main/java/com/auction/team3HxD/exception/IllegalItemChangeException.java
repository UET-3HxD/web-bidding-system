package com.auction.team3HxD.exception;

/**
 * Ném exception này khi cố thay đổi/chỉnh sửa sản phẩm đang được đấu giá
 */
public class IllegalItemChangeException extends Exception {

  public IllegalItemChangeException(String message) {
    super(message);
  }
}
