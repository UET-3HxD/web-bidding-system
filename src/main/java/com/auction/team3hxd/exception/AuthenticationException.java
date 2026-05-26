package com.auction.team3hxd.exception;

/**
 * Ném ra excecption này khi xác thực mật khẩu không chính xác
 */
public class AuthenticationException extends Exception {

  public AuthenticationException(String message) {
    super(message);
  }
}
