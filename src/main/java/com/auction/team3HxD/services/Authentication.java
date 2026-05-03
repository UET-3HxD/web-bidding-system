package com.auction.team3HxD.services;

import com.auction.team3HxD.exception.AuthenticationException;

public class Authentication {
    public boolean authenticatePassword(String password , String inputPassword) throws AuthenticationException {
        if (password.equals(inputPassword)) {
            return true;
        }
        else {
            throw new AuthenticationException("Wrong Password");
        }
    }
}
