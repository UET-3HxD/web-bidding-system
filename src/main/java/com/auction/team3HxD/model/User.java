package com.auction.team3HxD.model;

import com.auction.team3HxD.model.enums.Role;

public abstract class User extends Entity{
    protected String username;
    protected String password;
    protected String email;
    protected Role role;

    //Constructor
    public User(String username, String password, String email , Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    //Getter and setter
    public int getId() {
        return super.getId();
    }
    public String getPassword() {return password;}

    public String getEmail() {
        return email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

}
