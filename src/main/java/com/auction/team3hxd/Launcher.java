package com.auction.team3hxd;

import com.auction.team3hxd.network.AuctionServer;

public class Launcher {
    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> {
            try {
                System.out.println(">>> [SERVER] Đang khởi động...");
                AuctionServer.main(args);
            } catch (Exception e) {
                System.err.println(">>> [SERVER] Lỗi khởi động: " + e.getMessage());
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Main.main(args);
    }
}
