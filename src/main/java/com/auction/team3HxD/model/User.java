package com.auction.team3HxD.model;

import com.auction.team3HxD.model.enums.Role;

import java.util.UUID;

public abstract class User extends Entity{
    protected String userName;
    protected String passwordHash;
    protected String email;
    protected Role role;

    //Constructor
    public User(String userName , String passwordHash , String email , Role role) {
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.email = email;
        this.role = role;
    }

    //Getter and setter
    public UUID getId() {
        return super.getId();
    }
    public String getPasswordHash() {return passwordHash;}

    public String getEmail() {
        return email;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    //Authenciate
    public boolean authenticate(String passwordHash) {
        return this.passwordHash.equals(passwordHash);
    }
}
