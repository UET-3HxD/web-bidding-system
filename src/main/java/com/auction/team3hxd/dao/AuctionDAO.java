package com.auction.team3hxd.dao;

import com.auction.team3hxd.model.Auction;
import com.auction.team3hxd.model.Item;
import com.auction.team3hxd.model.User;
import com.auction.team3hxd.model.enums.AuctionStatus;
import com.auction.team3hxd.util.DBConnection;
import com.auction.team3hxd.dto.AuctionContextDTO;


import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

  private final UserDAO userDAO = new UserDAO();
  private final ItemDAO itemDAO = new ItemDAO();

  public int startAuction(int itemId, int durationMinutes) {
    String insertSessionSql =
        "INSERT INTO auction_sessions (item_id, start_time, end_time, current_price, status) " +
            "VALUES (?, NOW(), DATE_ADD(NOW(), INTERVAL ? MINUTE), 0, 'ACTIVE')";
    String updateItemSql = "UPDATE items SET status = 'LIVE' WHERE id = ?";

    Connection conn = null;
    int generatedAuctionId = -1;

    try {
      conn = com.auction.team3hxd.util.DBConnection.getConnection();
      conn.setAutoCommit(false);

      try (PreparedStatement pstmt1 = conn.prepareStatement(insertSessionSql,
          Statement.RETURN_GENERATED_KEYS)) {
        pstmt1.setInt(1, itemId);
        pstmt1.setInt(2, durationMinutes);
        pstmt1.executeUpdate();

        try (ResultSet rs = pstmt1.getGeneratedKeys()) {
          if (rs.next()) {
            generatedAuctionId = rs.getInt(1);
          }
        }
      }
      try (PreparedStatement pstmt2 = conn.prepareStatement(updateItemSql)) {
        pstmt2.setInt(1, itemId);
        pstmt2.executeUpdate();
      }

      conn.commit();
      return generatedAuctionId;

    } catch (SQLException e) {
      System.err.println(">>> startAuction ERROR: " + e.getMessage());
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      return -1;
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public List<Auction> findLiveAuctions() {
    List<Auction> list = new ArrayList<>();

    String sql =
        "SELECT a.*, (SELECT COUNT(*) FROM bids b WHERE b.auction_id = a.id) AS bid_count " +
            "FROM auction_sessions a " +
            "WHERE a.status = 'ACTIVE' AND a.end_time > NOW() " +
            "ORDER BY a.end_time ASC";

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        Auction auction = mapResultSetToAuction(rs);
        auction.setBidCount(rs.getInt("bid_count"));
        list.add(auction);
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return list;
  }


// helper method for place bid action

  public boolean lockAuctionSession(Connection conn, int auctionId) throws SQLException {
    String lockSql = "SELECT current_price FROM auction_sessions WHERE id = ? FOR UPDATE";
    try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
      ps.setInt(1, auctionId);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    }
  }

  public AuctionContextDTO getAuctionContext(Connection conn, int auctionId) throws SQLException {
    String sql = "SELECT a.status, TIMESTAMPDIFF(SECOND, NOW(), a.end_time) AS time_left, " +
        "i.product_name, i.starting_price, COALESCE(MAX(b.bid_amount), 0) AS current_highest_price "
        +
        "FROM auction_sessions a " +
        "JOIN items i ON a.item_id = i.id " +
        "LEFT JOIN bids b ON a.id = b.auction_id " +
        "WHERE a.id = ? " +
        "GROUP BY a.id, a.status, a.end_time, i.product_name, i.starting_price";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          AuctionContextDTO ctx = new AuctionContextDTO();
          ctx.setStatus(rs.getString("status"));
          ctx.setTimeLeft(rs.getInt("time_left"));
          ctx.setProductName(rs.getString("product_name"));
          ctx.setStartPrice(rs.getDouble("starting_price"));
          ctx.setCurrentHighestPrice(rs.getDouble("current_highest_price"));
          return ctx;
        }
      }
    }
    return null;
  }
  public void extendEndTime(Connection conn, int auctionId) throws SQLException {
    String sql = "UPDATE auction_sessions SET end_time = DATE_ADD(end_time, INTERVAL 5 MINUTE) WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      ps.executeUpdate();
    }
  }

  public void updateAuctionPrice(Connection conn, int auctionId, double bidAmount)
      throws SQLException {
    String sql = "UPDATE auction_sessions SET current_price = ? WHERE id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setDouble(1, bidAmount);
      ps.setInt(2, auctionId);
      ps.executeUpdate();
    }
  }

  public void insertBidHistory(Connection conn, int auctionId, int userId, double bidAmount)
      throws SQLException {
    String sql = "INSERT INTO bids (auction_id, user_id, bid_amount, bid_time) VALUES (?, ?, ?, NOW())";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      ps.setInt(2, userId);
      ps.setDouble(3, bidAmount);
      ps.executeUpdate();
    }
  }

  public java.util.List<Integer> getParticipantsByAuctionId(int auctionId) {
    java.util.List<Integer> userIds = new java.util.ArrayList<>();
    String sql = "SELECT DISTINCT user_id FROM bids WHERE auction_id = ?";

    try (Connection conn = com.auction.team3hxd.util.DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          userIds.add(rs.getInt("user_id"));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return userIds;
  }

  public Auction findById(int id) {
    String sql = "SELECT * FROM auction_sessions WHERE id = ?";

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToAuction(rs);
        }
      }
    } catch (SQLException e) {
      System.err.println("Lỗi khi tìm Auction theo ID: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  public String getHighestBidderName(int auctionId) {
    String sql = "SELECT u.username FROM bids b " +
        "JOIN users u ON b.user_id = u.id " +
        "WHERE b.auction_id = ? ORDER BY b.bid_amount DESC LIMIT 1";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("username");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public double getUserLastBid(int auctionId, int userId) {
    String sql = "SELECT MAX(bid_amount) FROM bids WHERE auction_id = ? AND user_id = ?";
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      ps.setInt(2, userId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getDouble(1);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return 0;
  }

  public List<String> getBidHistory(int userId) {
    List<String> historyList = new ArrayList<>();
    String sql =
        "SELECT a.id, i.product_name, i.item_type, i.image_path, " +
            "       i.starting_price, a.current_price, " +
            "       COALESCE(ub.max_bid, 0) AS user_bid, " +
            "       CASE " +
            "           WHEN a.end_time > NOW() THEN 'ACTIVE' " +
            "           WHEN (SELECT user_id FROM bids b2 WHERE b2.auction_id = a.id ORDER BY b2.bid_amount DESC LIMIT 1) = ? THEN 'WON' "
            +
            "           ELSE 'LOST' " +
            "       END AS bid_status " +
            "FROM auction_sessions a " +
            "JOIN items i ON a.item_id = i.id " +
            "LEFT JOIN ( " +
            "    SELECT auction_id, MAX(bid_amount) AS max_bid " +
            "    FROM bids " +
            "    WHERE user_id = ? " +
            "    GROUP BY auction_id " +
            ") ub ON a.id = ub.auction_id " +
            "WHERE EXISTS ( " +
            "    SELECT 1 FROM bids b WHERE b.auction_id = a.id AND b.user_id = ? " +
            ") " +
            "GROUP BY a.id, i.product_name, i.item_type, i.image_path, i.starting_price, a.current_price, a.end_time, ub.max_bid "
            +
            "ORDER BY " +
            "    CASE WHEN a.end_time > NOW() THEN 1 ELSE 2 END, " +
            // Đẩy phòng Đang diễn ra lên đầu
            "    a.end_time DESC"; // Sắp xếp theo thời gian mới nhất

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      // Tham số truyền vào giữ nguyên
      ps.setInt(1, userId);   // cho subquery thắng/thua
      ps.setInt(2, userId);   // cho subquery lấy giá cao nhất của user
      ps.setInt(3, userId);   // cho EXISTS

      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        // Định dạng chuỗi giữ nguyên để không làm vỡ giao diện
        String record = String.format("%d#%s#%s#%s#%.0f#%.0f#%.0f#%s",
            rs.getInt("id"),
            rs.getString("product_name"),
            rs.getString("item_type"),
            rs.getString("image_path") != null ? rs.getString("image_path") : "",
            rs.getDouble("starting_price"),
            rs.getDouble("current_price"),
            rs.getDouble("user_bid"),
            rs.getString("bid_status")
        );
        historyList.add(record);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return historyList;
  }

  private Auction mapResultSetToAuction(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    int itemId = rs.getInt("item_id");

    double currentPrice = rs.getDouble("current_price");
    if (rs.wasNull()) {
      currentPrice = 0;
    }

    AuctionStatus status = AuctionStatus.valueOf(rs.getString("status"));

    LocalDateTime startTime = rs.getTimestamp("start_time") != null ?
        rs.getTimestamp("start_time").toLocalDateTime() : LocalDateTime.now();
    LocalDateTime endTime = rs.getTimestamp("end_time") != null ?
        rs.getTimestamp("end_time").toLocalDateTime() : LocalDateTime.now().plusHours(1);

    Item item = itemDAO.findById(itemId);

    User seller = null;
    double bidIncrement = 0;
    if (item != null) {
      seller = userDAO.findById(item.getSellerId());
      // Tính bidIncrement = 2% giá khởi điểm (giống AuctionService)
      bidIncrement = item.getPrice() * 0.02;
    }

    double startPrice = (item != null) ? item.getPrice() : 0.0;

    Auction auction = new Auction(
        id, seller, item, startPrice, currentPrice, bidIncrement,
        status, startTime, endTime
    );

    if (auction.getStatus() == AuctionStatus.ACTIVE && LocalDateTime.now()
        .isAfter(auction.getEndTime())) {
      auction.endAuction();
    }
    return auction;
  }

  public int countLiveAuctions() {
    String sql = "SELECT COUNT(*) FROM auction_sessions WHERE end_time > NOW()";
    int count = 0;
    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {
      if (rs.next()) {
        count = rs.getInt(1);
      }

    } catch (SQLException e) {
      System.err.println(">>> [LỖI DB] Không thể đếm số cuộc đấu giá: " + e.getMessage());
      e.printStackTrace();
    }
    return count;
  }

  /**
   * Lấy lịch sử đấu giá của một phiên (tất cả các lần đặt giá). Trả về danh sách chuỗi:
   * "HH:mm:ss#price"
   */
  public List<String> getAuctionBidHistory(int auctionId) {
    List<String> history = new ArrayList<>();
    String sql = "SELECT bid_amount, bid_time FROM bids WHERE auction_id = ? ORDER BY bid_time ASC";

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, auctionId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          double price = rs.getDouble("bid_amount");
          Timestamp ts = rs.getTimestamp("bid_time");
          String timeStr = ts.toLocalDateTime()
              .toLocalTime()
              .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
          history.add(timeStr + "#" + String.format("%.0f", price));
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return history;
  }
}