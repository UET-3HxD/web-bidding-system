package com.auction.team3hxd.model;

import com.auction.team3hxd.model.enums.Role;

/**
 * Lớp đại diện cho một người dùng trong hệ thống (NormalUser và Admin).
 *
 * @author Huy
 *
 */
public class User extends Entity {

    protected String username;
    protected String password;
    protected String email;
    protected Role role;

    /**
     * Phương thức khởi tạo đối tượng người dùng mới.
     *
     * @param userName tên người dùng
     * @param password mật khẩu
     * @param email    địa chỉ email
     * @param role     vai trò (USER / ADMIN)
     */
    public User(String userName, String password, String email, Role role) {
        this.username = userName;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getter and setter
    public int getId() {
        return super.getId();
    }

    public String getPassword() {
        return password;
    }

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
