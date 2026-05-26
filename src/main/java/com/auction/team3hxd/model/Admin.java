package com.auction.team3hxd.model;

import com.auction.team3hxd.model.enums.Role;

/**
 * Lớp đại diện cho người dùng với vai trò Admin.
 *
 * @author Huy
 * */
public class Admin extends User {

  /**
   * Phương thức khởi tạo.
   *
   * @param userName tên
   * @param passwordHash mật khẩu
   * @param email địa chỉ email
   * @param role vai trò
   */
  public Admin(String userName, String passwordHash, String email, Role role) {
    super(userName, passwordHash, email, role);
  }
}
