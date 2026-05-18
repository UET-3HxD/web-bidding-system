package com.auction.team3HxD.network;

import com.auction.team3HxD.dao.AuctionDAO;
import com.auction.team3HxD.dao.ItemDAO;
import com.auction.team3HxD.dao.UserDAO;
import com.auction.team3HxD.model.Electronic;
import com.auction.team3HxD.model.Item;
import com.auction.team3HxD.services.AuctionService;
import com.auction.team3HxD.services.UserService;
import com.auction.team3HxD.util.AppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import static com.auction.team3HxD.network.AuctionServer.broadcast;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private final UserService userService = new UserService();
    private ItemDAO itemDAO = new ItemDAO();
    private UserDAO userDAO = new UserDAO(); // Để dùng cho login
    private AuctionDAO auctionDAO = new AuctionDAO();
    private AuctionService auctionService = new AuctionService();
    private int currentUserId = -1;

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
                            isAuthenticated = true;
                            out.println(res); // gửi LOGIN_OK|ROLE
                            broadcast("INFO|" + clientName + " đã tham gia phòng!");
                        } else {
                            out.println(res); // gửi mã lỗi
                        }
                    } else {}
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
                        }
                        else if (cmd.equals("CHANGE_EMAIL")) {
                            String ceRes = userService.changeEmail(this.clientName, parts[1], parts[2]);
                            out.println(ceRes);
                            break;
                        }
                        else if(cmd.equals("LOGOUT")) {
                            isAuthenticated = false;
                            break;
                        }
                        else if(cmd.equals("CREATE_ITEM")) {
                            try {
                                // bóc tách: [1]name, [2]price, [3]type, [4]desc, [5]path
                                String name = parts[1];
                                double price = Double.parseDouble(parts[2]);
                                String type = parts[3];
                                String desc = parts[4];
                                String path = parts[5];

                                // Gọi Service để xử lý (Service sẽ gọi DAO)
                                boolean success = userService.createItem(currentUserId, name, price, type, desc, path);

                                if (success) {
                                    out.println("CREATE_ITEM_SUCCESS");
                                } else {
                                    out.println("CREATE_ITEM_ERR|Lỗi khi lưu vào Database");
                                }
                            } catch (Exception e) {
                                out.println("CREATE_ITEM_ERR|Dữ liệu không hợp lệ");
                            }
                        }
                        else if (cmd.equals("GET_MY_ITEMS")) {
                            String response = userService.getMyItemsList(currentUserId);
                            out.println(response);
                        }
                        else if (cmd.equals("UPDATE_ITEM")) {
                            int itemId = Integer.parseInt(parts[1]);
                            String name = parts[2];
                            double price = Double.parseDouble(parts[3]);
                            String desc = parts[4];

                            boolean success = userService.updateItem(itemId, name, price, desc);
                            out.println(success ? "UPDATE_ITEM_SUCCESS" : "UPDATE_ITEM_ERR|Không thể cập nhật sản phẩm này");
                        }
                        else if (cmd.equals("DELETE_ITEM")) {
                            int itemId = Integer.parseInt(parts[1]);
                            boolean success = userService.deleteItem(itemId);

                            if (success) {
                                out.println("DELETE_ITEM_SUCCESS");
                            } else {
                                out.println("DELETE_ITEM_ERR|Sản phẩm không tồn tại hoặc không thể xóa");
                            }
                        }
                        else if(cmd.equals("START_AUCTION")) {
                            try {
                                int auctionItemId = Integer.parseInt(parts[1]);
                                int minutes = Integer.parseInt(parts[2]);

                                System.out.println(">>> DEBUG: START_AUCTION received, itemId=" + auctionItemId + ", minutes=" + minutes);
                                System.out.flush();

                                boolean isStarted = userService.startAuction(auctionItemId, minutes);

                                System.out.println(">>> DEBUG: startAuction result=" + isStarted);
                                System.out.flush();

                                if (isStarted) {
                                    out.println("START_AUCTION_SUCCESS");
                                } else {
                                    out.println("START_AUCTION_ERR|Lỗi hệ thống khi khởi tạo phiên đấu giá");
                                }
                                out.flush();
                            } catch (Exception e) {
                                System.out.println(">>> DEBUG: Exception in START_AUCTION: " + e.getMessage());
                                e.printStackTrace();
                                out.println("START_AUCTION_ERR|Dữ liệu không hợp lệ");
                                out.flush();
                            }
                        }
                        else if (cmd.equals("GET_LIVE_AUCTIONS")) {
                            String response = userService.getLiveAuctionsMessage();
                            out.println(response);
                        }
                        // ===== ADMIN COMMANDS =====
                        else if (cmd.equals("GET_PENDING_ITEMS")) {
                            try {
                                if (!userService.isAdmin(currentUserId)) {
                                    out.println("ERR|Bạn không có quyền thực hiện thao tác này!");
                                    continue;
                                }
                                String response = userService.getPendingItemsList();
                                out.println(response);
                            } catch (Exception e) {
                                out.println("PENDING_ITEMS_EMPTY");
                            }
                        }
                        else if (cmd.equals("APPROVE_ITEM")) {
                            try {
                                if (!userService.isAdmin(currentUserId)) {
                                    out.println("ERR|Bạn không có quyền!");
                                    continue;
                                }
                                int itemId = Integer.parseInt(parts[1]);
                                boolean success = userService.approveItem(itemId);
                                if (success) {
                                    out.println("APPROVE_SUCCESS");
                                } else {
                                    out.println("APPROVE_ERR|Không thể duyệt sản phẩm này");
                                }
                            } catch (Exception e) {
                                out.println("APPROVE_ERR|Dữ liệu không hợp lệ");
                            }
                        }
                        else if (cmd.equals("REJECT_ITEM")) {
                            try {
                                if (!userService.isAdmin(currentUserId)) {
                                    out.println("ERR|Bạn không có quyền!");
                                    continue;
                                }
                                int itemId = Integer.parseInt(parts[1]);
                                boolean success = userService.rejectItem(itemId);
                                if (success) {
                                    out.println("REJECT_SUCCESS");
                                } else {
                                    out.println("REJECT_ERR|Không thể từ chối sản phẩm này");
                                }
                            } catch (Exception e) {
                                out.println("REJECT_ERR|Dữ liệu không hợp lệ");
                            }
                        }
                        else if (cmd.equals("BID") || cmd.equals("CHAT")) {
                            handleAuctionCommands(message);
                        }
                        else if (cmd.equals("PLACE_BID")) {
                            try {
                                int bidAuctionId = Integer.parseInt(parts[1]);
                                int bidUserId = currentUserId;
                                double bidAmount = Double.parseDouble(parts[3]);

                                // Gọi hàm Transaction dưới DAO
                                String result = auctionDAO.placeBidTransaction(bidAuctionId, bidUserId, bidAmount);

                                if (result.startsWith("SUCCESS")) {
                                    // 1. Phản hồi cho người vừa đặt giá: BẠN ĐÃ THÀNH CÔNG
                                    out.println("BID_SUCCESS");
                                    out.flush();

                                    // 2. Tách dữ liệu từ chuỗi kết quả: SUCCESS|newPrice|bidderName
                                    String[] resParts = result.split("\\|");
                                    String newPrice = resParts[1];
                                    String bidderName = resParts[2];

                                    // 3. BROADCAST: Báo cáo biến động giá cho TOÀN BỘ Client đang kết nối
                                    broadcast("BID_UPDATE|" + bidAuctionId + "#" + newPrice + "#" + bidderName);

                                } else {
                                    // Phản hồi lỗi: Trả lại đúng thông báo lỗi từ DAO
                                    out.println("BID_ERROR|" + result.split("\\|")[1]);
                                    out.flush();
                                }

                            } catch (Exception e) {
                                out.println("BID_ERROR|Dữ liệu gửi lên không hợp lệ!");
                                out.flush();
                            }
                        }
                        else if (cmd.equals("GET_AUCTION_DETAIL")){
                            try {
                                int aId = Integer.parseInt(parts[1]);
                                int uId = Integer.parseInt(parts[2]);
                                String detailMessage = auctionService.getAuctionDetailMessage(aId, uId);
                                out.println("AUCTION_DETAIL_SUCCESS|" + detailMessage);

                                if (detailMessage != null) {
                                    out.println("AUCTION_DETAIL_SUCCESS|" + detailMessage);
                                } else {
                                    out.println("AUCTION_DETAIL_ERROR|Không tìm thấy thông tin phiên đấu giá!");
                                }
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
            // Mất kết nối đột ngột
        } finally {
            userService.logout(clientName);
            closeConnection();
        }
    }

    private void handleAuctionCommands(String message) {
        AuctionService auctionService = new AuctionService();
        String[] parts = message.split("\\|");
        String cmd = parts[0];

        switch (cmd) {
            case "BID":
                double amount = Double.parseDouble(parts[1]);
                String result = auctionService.placeBid(this.clientName, amount);
                if (!result.equals("BID_SUCCESS")) {
                    out.println(result); // gửi lỗi riêng cho người bid
                }
                break;

            case "CHAT":
                String content = parts[1];
                broadcast("CHAT|" + this.clientName + ": " + content);
                break;

            default:
                out.println("ERR|Lệnh không hợp lệ");
        }
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
