package com.auction.team3hxd;

import com.auction.team3hxd.network.AuctionServer;

public class Launcher {
    public static void main(String[] args) {
        boolean isClientOnly = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--client-only")) {
                isClientOnly = true;
                break;
            }
        }
        if (!isClientOnly) {
            System.out.println("=========ĐANG KHỞI ĐỘNG HỆ THỐNG (SERVER + CLIENT)=========");

            Thread serverThread = new Thread(() -> {
                try {
                    AuctionServer.main(args);
                } catch (Exception e) {
                    System.err.println(">>> [SERVER] Không thể khởi động: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        } else {
            System.out.println("=========CHẾ ĐỘ CLIENT-ONLY=========");
        }
        System.out.println(">>> [CLIENT] Đang hiển thị giao diện...");
        Main.main(args);
    }
}
