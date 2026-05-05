package com.auction.team3HxD.network;

import com.auction.team3HxD.services.AuctionService;
import com.auction.team3HxD.services.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;
    private final UserService userService = new UserService();

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
                            isAuthenticated = true;
                            out.println(res); // gửi LOGIN_OK|ROLE
                            AuctionServer.broadcast("INFO|" + clientName + " đã tham gia phòng!");
                        } else {
                            out.println(res); // gửi mã lỗi
                        }
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
                        handleAuctionCommands(message);
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
                AuctionServer.broadcast("CHAT|" + this.clientName + ": " + content);
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