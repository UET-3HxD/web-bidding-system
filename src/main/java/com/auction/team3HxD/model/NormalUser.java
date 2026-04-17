package com.auction.team3HxD.model;

import com.auction.team3HxD.model.enums.Role;

public class NormalUser extends User {

    public NormalUser(String userName, String passwordHash, String email, Role role) {
        super(userName, passwordHash, email, role);
    }
}