package com.auction.team3hxd.network;

import com.auction.team3hxd.dao.AuctionDAO;
import com.auction.team3hxd.dao.ItemDAO;
import com.auction.team3hxd.dao.UserDAO;
import com.auction.team3hxd.model.observer.ClientObserver;
import com.auction.team3hxd.model.observer.NotificationManager;
import com.auction.team3hxd.services.AuctionService;
import com.auction.team3hxd.services.UserService;
import com.auction.team3hxd.services.ItemService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.auction.team3hxd.network.AuctionServer.broadcast;

public class ClientHandler implements Runnable, ClientObserver {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private final UserService userService = new UserService();
    private final ItemService itemService = new ItemService();
    private ItemDAO itemDAO = new ItemDAO();
    private UserDAO userDAO = new UserDAO(); // Để dùng cho login
    private AuctionDAO auctionDAO = new AuctionDAO();
    private AuctionService auctionService = new AuctionService();
    private int currentUserId = -1;
    public static final Map<Integer, ClientHandler> activeClients = new ConcurrentHashMap<>();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String message;
            boolean isAuthenticated = false;

            // ===== Vòng lặp chờ xác thực (LOGIN/REGISTER) =====
            while (!isAuthenticated && (message = in.readLine()) != null) {
                System.out.println(">>> Received: " + message); // thêm để debug
                String[] parts = message.split("\\|");
                String cmd = parts[0];

                try {
                    if (cmd.equals("REGISTER")) {
                        System.out.println("Calling userService.register...");
                        System.out.flush();
                        String res = userService.register(parts[1], parts[2], parts[3]);
                        System.out.println("Register result: " + res);
                        System.out.flush();
                        out.println(res);
                        System.out.println("Sent response to client: " + res);
                        System.out.flush();
                    } else if (cmd.equals("LOGIN")) {
                        String res = userService.login(parts[1], parts[2]);
                        if (res.startsWith("LOGIN_OK")) {
                            this.clientName = parts[1];
                            this.currentUserId = userDAO.getUserByUsername(this.clientName).getId();
                            NotificationManager.getInstance().addObserver(this);
                            ClientHandler.addActiveClient(this.currentUserId, this);   // 👈 THÊM DÒNG NÀY
                            isAuthenticated = true;
                            out.println(res);
                            broadcast("INFO|" + clientName + " đã tham gia phòng!");
                        } else {
                            out.println(res);
                        }
                    } else {
                    }
                } catch (Exception e) {
                    // Bắt mọi lỗi (SQL, logic...) để client không bị treo
                    out.println("ERR|Server error: " + e.getMessage());
                    e.printStackTrace();
                    break; // đóng kết nối sau khi gửi lỗi
                }
            }

            // ===== Sau khi đăng nhập thành công, xử lý lệnh đấu giá =====
            if (isAuthenticated) {
                while ((message = in.readLine()) != null) {
                    try {
                        System.out.println(">>> Received: " + message); // thêm để debug
                        String[] parts = message.split("\\|");
                        String cmd = parts[0];
                        if (cmd.equals("CHANGE_PASSWORD")) {
                            String cpRes = userService.changePassword(this.clientName, parts[1], parts[2]);
                            out.println(cpRes);
                        } else if (cmd.equals("CHANGE_EMAIL")) {
                            String ceRes = userService.changeEmail(this.clientName, parts[1], parts[2]);
                            out.println(ceRes);
                        } else if (cmd.equals("LOGOUT")) {
                            isAuthenticated = false;
                            break;
                        } else if (cmd.equals("CREATE_ITEM")) {
                            try {
                                // bóc tách: [1]name, [2]price, [3]type, [4]desc, [5]path
                                String name = parts[1];
                                double price = Double.parseDouble(parts[2]);
                                String type = parts[3];
                                String desc = parts[4];
                                String path = parts[5];

                                int newGeneratedId = itemService.createItem(currentUserId, name, price, type, desc, path);

                                if (newGeneratedId > 0) {
                                    out.println("CREATE_ITEM_SUCCESS");
                                    String payload = newGeneratedId + "|" + name;
                                    com.auction.team3hxd.model.observer.NotificationManager.getInstance()
                                            .notifyAllObservers("PRODUCT_SUBMITTED", payload);
                                } else {
                                    out.println("CREATE_ITEM_ERR|Lỗi khi lưu vào Database");
                                }
                            } catch (Exception e) {
                                out.println("CREATE_ITEM_ERR|Dữ liệu không hợp lệ");
                            }
                        } else if (cmd.equals("GET_MY_ITEMS")) {
                            String response = itemService.getMyItemsList(currentUserId);
                            out.println(response);
                        } else if (cmd.equals("UPDATE_ITEM")) {
                            System.out.println(">>> [SERVER DEBUG] Đã nhận được lệnh UPDATE_ITEM từ Client!");
                            int itemId = Integer.parseInt(parts[1]);
                            String name = parts[2];
                            double price = Double.parseDouble(parts[3]);
                            String desc = parts[4];

                            int success = itemService.updateItem(itemId, name, price, desc);
                            System.out.println(">>> [SERVER DEBUG] trạng thái update: " + success);
                            if (success > 0) {
                                System.out.println(">>> [SERVER DEBUG] Đã kích hoạt lệnh notifyAllObservers sang Admin.");
                                com.auction.team3hxd.model.observer.NotificationManager.getInstance()
                                        .notifyAllObservers("PRODUCT_SUBMITTED", "");
                            }
                            out.println(success > 0 ? "UPDATE_ITEM_SUCCESS" : "UPDATE_ITEM_ERR|Không thể cập nhật sản phẩm này");
                        } else if (cmd.equals("DELETE_ITEM")) {
                            int itemId = Integer.parseInt(parts[1]);
                            boolean success = itemService.deleteItem(itemId);

                            if (success) {
                                com.auction.team3hxd.model.observer.NotificationManager.getInstance()
                                        .notifyAllObservers("PRODUCT_SUBMITTED", "");
                                out.println("DELETE_ITEM_SUCCESS");
                            } else {
                                out.println("DELETE_ITEM_ERR|Sản phẩm không tồn tại hoặc không thể xóa");
                            }
                        } else if (cmd.equals("START_AUCTION")) {
                            try {
                                int auctionItemId = Integer.parseInt(parts[1]);
                                int minutes = Integer.parseInt(parts[2]);

                                int newAuctionId = userService.startAuction(auctionItemId, minutes);

                                if (newAuctionId > 0) {
                                    out.println("START_AUCTION_SUCCESS");
                                    com.auction.team3hxd.model.observer.NotificationManager.getInstance()
                                            .notifyOthers("NEW_AUCTION_ARRIVED", String.valueOf(newAuctionId), this.currentUserId);
                                } else {
                                    out.println("START_AUCTION_ERR|Lỗi hệ thống khi khởi tạo phiên đấu giá");
                                }
                                out.flush();
                            } catch (Exception e) {
                                System.out.println(">>> DEBUG: Exception in START_AUCTION: " + e.getMessage());
                                e.printStackTrace();
                                out.println("START_AUCTION_ERR|Dữ liệu không hợp lệ");
                            }
                        } else if (cmd.equals("GET_LIVE_AUCTIONS")) {
                            String response = userService.getLiveAuctionsMessage();
                            out.println(response);
                        } else if (cmd.equals("GET_BID_HISTORY")) {
                            try {
                                int historyUserId = this.currentUserId;
                                List<String> historyRecords = auctionDAO.getBidHistory(historyUserId);
                                if (historyRecords.isEmpty()) {
                                    out.println("BID_HISTORY_SUCCESS|EMPTY");
                                } else {
                                    String payload = String.join("|", historyRecords);
                                    out.println("BID_HISTORY_SUCCESS|" + payload);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (cmd.equals("CHAT")) {
                            handleAuctionCommands(message);
                        } else if (cmd.equals("PLACE_BID")) {
                            try {
                                int bidAuctionId = Integer.parseInt(parts[1]);
                                int bidUserId = currentUserId;
                                double bidAmount = Double.parseDouble(parts[3]);

                                String result = auctionService.placeBid(bidAuctionId, bidUserId, bidAmount);
                                out.println(result);
                                if (result.startsWith("BID_SUCCESS")) {
                                    String payload = bidAuctionId + "|" + bidAmount;
                                    com.auction.team3hxd.model.observer.NotificationManager.getInstance()
                                            .notifyOthers("BID_UPDATE", payload, this.currentUserId);
                                    // chuc nang nang cao: anti-snipe
                                }
                            } catch (Exception e) {
                                out.println("BID_ERROR|Dữ liệu gửi lên không hợp lệ!");
                                e.printStackTrace();
                            }
                        } else if (cmd.equals("GET_AUCTION_DETAIL")) {
                            try {
                                int aId = Integer.parseInt(parts[1]);
                                int uId = Integer.parseInt(parts[2]);
                                String detailMessage = auctionService.getAuctionDetailMessage(aId, uId);

                                if (detailMessage != null) {
                                    out.println("AUCTION_DETAIL_SUCCESS|" + detailMessage);
                                } else {
                                    out.println("AUCTION_DETAIL_ERROR|Không tìm thấy thông tin phiên đấu giá!");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // ===== LỊCH SỬ BID CHO BIỂU ĐỒ =====
                        else if (cmd.equals("GET_AUCTION_BID_HISTORY")) {
                            try {
                                int auctionId = Integer.parseInt(parts[1]);
                                List<String> history = auctionDAO.getAuctionBidHistory(auctionId);
                                if (history.isEmpty()) {
                                    out.println("AUCTION_BID_HISTORY|EMPTY");
                                } else {
                                    String payload = String.join("|", history);
                                    out.println("AUCTION_BID_HISTORY|" + payload);
                                }
                            } catch (Exception e) {
                                out.println("AUCTION_BID_HISTORY|EMPTY");
                            }
                        } else if (cmd.equals("CHECK_AUCTION_STATUS")) {
                            try {
                                int auctionId = Integer.parseInt(parts[1]);
                                String sql = "SELECT a.status, a.end_time, i.product_name FROM auction_sessions a " +
                                        "JOIN items i ON a.item_id = i.id WHERE a.id = ?";

                                try (Connection conn = com.auction.team3hxd.util.DBConnection.getConnection();
                                        PreparedStatement ps = conn.prepareStatement(sql)) {
                                    ps.setInt(1, auctionId);
                                    try (ResultSet rs = ps.executeQuery()) {
                                        if (rs.next()) {
                                            String status = rs.getString("status");
                                            java.sql.Timestamp endTime = rs.getTimestamp("end_time");
                                            String productName = rs.getString("product_name");
                                            if (status.equals("ACTIVE") && endTime.after(new java.sql.Timestamp(System.currentTimeMillis()))) {

                                                java.util.List<Integer> participantIds = auctionDAO.getParticipantsByAuctionId(auctionId);
                                                String extendPayload = auctionId + "|" + productName;
                                                com.auction.team3hxd.model.observer.NotificationManager.getInstance()
                                                        .notifySpecificGroup("AUCTION_EXTENDED", extendPayload, participantIds);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // ===== ADMIN COMMANDS =====
                        else if (cmd.equals("GET_PENDING_ITEMS")) {
                            try {
                                if (!userService.isAdmin(currentUserId)) {
                                    out.println("ERR|Bạn không có quyền thực hiện thao tác này!");
                                    continue;
                                }
                                String response = itemService.getPendingItemsList();
                                out.println(response);
                            } catch (Exception e) {
                                out.println("PENDING_ITEMS_EMPTY");
                            }
                        } else if (cmd.equals("APPROVE_ITEM")) {
                            try {
                                if (!userService.isAdmin(currentUserId)) {
                                    out.println("ERR|Bạn không có quyền!");
                                    continue;
                                }
                                int itemId = Integer.parseInt(parts[1]);
                                int ownerId = itemDAO.getOwnerIdByItemId(itemId);
                                boolean success = itemService.approveItem(itemId);
                                if (success) {
                                    out.println("APPROVE_SUCCESS");
                                    if (ownerId != -1) {
                                        String payload = itemId + "|APPROVED";
                                        com.auction.team3hxd.model.observer.NotificationManager.getInstance()
                                                .notifySingleUser("MY_PRODUCT_STATUS_CHANGED", payload, ownerId);
                                    }
                                } else {
                                    out.println("APPROVE_ERR|Không thể duyệt sản phẩm này");
                                }
                            } catch (Exception e) {
                                out.println("APPROVE_ERR|Dữ liệu không hợp lệ");
                            }
                        } else if (cmd.equals("REJECT_ITEM")) {
                            try {
                                if (!userService.isAdmin(currentUserId)) {
                                    out.println("ERR|Bạn không có quyền!");
                                    continue;
                                }
                                int itemId = Integer.parseInt(parts[1]);
                                int ownerId = itemDAO.getOwnerIdByItemId(itemId);
                                boolean success = itemService.rejectItem(itemId);
                                if (success) {
                                    out.println("REJECT_SUCCESS");
                                    if (ownerId != -1) {
                                        String payload = itemId + "|REJECTED";
                                        com.auction.team3hxd.model.observer.NotificationManager.getInstance()
                                                .notifySingleUser("MY_PRODUCT_STATUS_CHANGED", payload, ownerId);
                                    }

                                } else {
                                    out.println("REJECT_ERR|Không thể từ chối sản phẩm này");
                                }
                            } catch (Exception e) {
                                out.println("REJECT_ERR|Dữ liệu không hợp lệ");
                            }
                        }
                        // ===== USER MANAGEMENT (ADMIN) =====
                        else if (cmd.equals("GET_ALL_USERS")) {
                            try {
                                if (!userService.isAdmin(currentUserId)) {
                                    out.println("ERR|Bạn không có quyền!");
                                    continue;
                                }
                                String response = userService.getAllUsersList();
                                out.println(response);
                            } catch (Exception e) {
                                out.println("ALL_USERS_EMPTY");
                            }
                        } else if (cmd.equals("UNBAN_USER")) {
                            try {
                                if (!userService.isAdmin(currentUserId)) {
                                    out.println("ERR|Bạn không có quyền!");
                                    continue;
                                }
                                int targetUserId = Integer.parseInt(parts[1]);
                                boolean success = userService.unbanUser(targetUserId);
                                if (success) {
                                    out.println("UNBAN_SUCCESS");
                                } else {
                                    out.println("UNBAN_ERR|Không thể mở khóa tài khoản");
                                }
                            } catch (Exception e) {
                                out.println("UNBAN_ERR|Dữ liệu không hợp lệ");
                            }
                        } else if (cmd.equals("BAN_USER")) {
                            try {
                                int targetUserId = Integer.parseInt(parts[1]);
                                boolean success = userDAO.updateRole(targetUserId, "BANNED");
                                if (success) {
                                    out.println("ADMIN_BAN_SUCCESS|Đã khóa tài khoản thành công.");
                                    ClientHandler targetClient = activeClients.get(targetUserId);
                                    if (targetClient != null) {
                                        targetClient.sendKickMessage();
                                    }
                                } else {
                                    out.println("ADMIN_BAN_ERROR|Lỗi khi khóa tài khoản.");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (cmd.equals("GET_ADMIN_DASHBOARD")) {
                            try {
                                int pendingCount = itemDAO.countPendingItems();
                                int liveCount = auctionDAO.countLiveAuctions();
                                int userCount = userDAO.countTotalUsers();

                                // 2. Tính thời gian hoạt động (Uptime)
                                long uptimeMillis = System.currentTimeMillis() - AuctionServer.START_TIME;
                                long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(uptimeMillis);
                                long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60;
                                String uptimeStr = hours + " giờ " + minutes + " phút";

                                // 3. Gửi trả kết quả về Client
                                out.println("ADMIN_DASHBOARD_SUCCESS|" + pendingCount + "|" + liveCount + "|" + userCount + "|" + uptimeStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        out.println("ERR|" + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println(">>> [LỖI SERVER CRASH LUỒNG CỦA USER " + this.currentUserId + "]:");
            e.printStackTrace();
        } finally {
            userService.logout(clientName);
            NotificationManager.getInstance().removeObserver(this);
            removeActiveClient(currentUserId);
            closeConnection();
        }
    }

    @Override
    public void onNotify(String eventType, String fullMessage) {
        // Mỗi khi NotificationManager gọi hàm này, đẩy tin nhắn qua Socket về cho Client
        try {
            if (out != null) {
                out.println(fullMessage);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getObserverUserId() {
        return this.currentUserId;
    }

    private void handleAuctionCommands(String message) {
        AuctionService auctionService = new AuctionService();
        String[] parts = message.split("\\|");
        String cmd = parts[0];

        switch (cmd) {
            case "CHAT":
                String content = parts[1];
                broadcast("CHAT|" + this.clientName + ": " + content);
                break;

            default:
                out.println("ERR|Lệnh không hợp lệ");
        }
    }

    public static void addActiveClient(int userId, ClientHandler handler) {
        activeClients.put(userId, handler);
    }

    public static void removeActiveClient(int userId) {
        activeClients.remove(userId);
    }

    public void sendKickMessage() {
        out.println("YOU_ARE_BANNED|Tài khoản của bạn vừa bị khóa!");
        out.flush();
    }

    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
