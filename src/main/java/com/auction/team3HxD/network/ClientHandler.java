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

            while (!isAuthenticated && (message = in.readLine()) != null) {
                String[] parts = message.split("\\|"); // Thống nhất dùng dấu gạch đứng |
                String cmd = parts[0];

                if (cmd.equals("REGISTER")) {
                    String res = userService.register(parts[1], parts[2], parts[3]);
                    out.println(res); // Trả về REG_SUCCESS hoặc lỗi
                }
                else if (cmd.equals("LOGIN")) {
                    String res = userService.login(parts[1], parts[2]);
                    if (res.equals("LOGIN_SUCCESS")) {
                        this.clientName = parts[1];
                        isAuthenticated = true;
                        out.println("LOGIN_SUCCESS");
                        // Thông báo cho mọi người có thành viên mới
                        AuctionServer.broadcast("INFO|" + clientName + " đã tham gia phòng!");
                    } else {
                        out.println(res);
                    }
                }
            }

            // CHỈ KHI LOGIN THÀNH CÔNG MỚI CHẠY TIẾP ĐỂ NHẬN LỆNH BID
            if (isAuthenticated) {
                while ((message = in.readLine()) != null) {
                    handleAuctionCommands(message); // Logic đấu giá bạn đã viết ở trên
                }
            }

        } catch (IOException e) {
            // Xử lý khi mất kết nối
        } finally {
            userService.logout(clientName);
            closeConnection();
        }
    }

    private void handleAuctionCommands(String message) {
        AuctionService auctionService = new AuctionService();
        try {
            String[] parts = message.split("\\|");
            String cmd = parts[0];

            switch (cmd) {
                case "BID" -> { //cmd gửi về là bid
                    double amount = Double.parseDouble(parts[1]);

                    // xử lý bid
                    String result = auctionService.placeBid(this.clientName, amount);

                    // trả kq bid chỉ cho nguười bid
                    if (!result.equals("BID_SUCCESS")) {
                        out.println(result);
                    }
                }

                case "CHAT" -> { //cmd gửi về là chat
                    String content = parts[1];
                    AuctionServer.broadcast("CHAT|" + this.clientName + ": " + content);
                }

                default -> out.println("ERR|Lệnh không hợp lệ");
            }
        } catch (Exception e) {
            out.println("ERR|Sai định dạng dữ liệu");
        }
    }

    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    private void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
