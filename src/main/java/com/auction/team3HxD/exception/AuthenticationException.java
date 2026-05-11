package com.auction.team3HxD.exception;

/**
 * Ném ra excecption này khi xác thực mật khẩu không chính xác
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
