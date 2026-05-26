package com.auction.team3hxd.services;

import com.auction.team3hxd.dto.AuctionContextDTO;

public class BidValidator {

  public String validate(AuctionContextDTO ctx, double bidAmount) {
    if (!"ACTIVE".equals(ctx.getStatus()) || ctx.getTimeLeft() <= 0) {
      return "BID_ERROR|Phiên đấu giá đã kết thúc!";
    }

    double minIncrement = ctx.getStartPrice() * 0.02;

    if (ctx.getCurrentHighestPrice() == 0) {
      // Lượt đấu giá đầu tiên
      if (bidAmount < ctx.getStartPrice()) {
        return "BID_ERROR|Lượt ra giá đầu tiên phải lớn hơn hoặc bằng giá khởi điểm (" + String.format("%,.0f", ctx.getStartPrice()) + " VNĐ)!";
      }
    } else {
      // Các lượt đấu giá tiếp theo
      if (bidAmount <= ctx.getCurrentHighestPrice()) {
        return "BID_ERROR|Mức giá phải cao hơn giá hiện tại!";
      }
      if (bidAmount < (ctx.getCurrentHighestPrice() + minIncrement)) {
        return "BID_ERROR|Mức giá phải cộng thêm ít nhất bước giá tối thiểu (" + String.format("%,.0f", minIncrement) + " VNĐ)!";
      }
    }

    return null; // Trả về null nghĩa là dữ liệu HỢP LỆ
  }
}
