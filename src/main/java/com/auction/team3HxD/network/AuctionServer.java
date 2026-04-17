package com.auction.team3HxD.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionServer {
    private static final int PORT = 5000;
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static double currentPrice = 0;
    private static String topBidder = "None";

    private static int timeLeft = 300;
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startCountdown() {
        scheduler.scheduleAtFixedRate(() -> {
            if (timeLeft > 0) {
                timeLeft--;
                // Gửi thời gian mới cho tất cả Client
                broadcast("TIME:" + timeLeft);
            } else {
                stopAuction();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static void stopAuction() {
        scheduler.shutdown();
        broadcast("END:Winner is " + topBidder + " with " + currentPrice + "$");
        System.out.println("Auction ended!");
    }
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Auction Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New member joined: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // xử lý tranh chấp
    public static synchronized void handleBid(String bidderName, double bidAmount) {
        if (bidAmount > currentPrice && timeLeft > 0) {
            currentPrice = bidAmount;
            topBidder = bidderName;

            // extend bid nếu có người snip
            if (timeLeft < 10) {
                timeLeft += 30;
                System.out.println("Time extended due to late bid!");
            }

            broadcast("NEW_BID:" + bidderName + ":" + currentPrice);
        } else {
            // Gửi thông báo riêng cho người bid lỗi...
        }
    }

    // thông báo
    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
