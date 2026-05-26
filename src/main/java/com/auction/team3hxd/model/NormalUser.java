package com.auction.team3hxd.model;

import com.auction.team3hxd.model.enums.Role;

/**
 *  Lớp đại diện cho người sử dụng hệ thống.
 *
 *  @author Hieu
 *  */
public class NormalUser extends User {

  /**
   * Phương thức khởi tạo đối tượng.
   *
   * @param userName tên người dùng
   * @param passwordHash mật khẩu
   * @param email địa chỉ email
   * @param role vai trò
   */
  public NormalUser(String userName, String passwordHash, String email, Role role) {
    super(userName, passwordHash, email, role);
  }
}
