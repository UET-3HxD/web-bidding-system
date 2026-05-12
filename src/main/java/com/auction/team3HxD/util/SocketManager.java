package com.auction.team3HxD.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Quản lý kết nối Socket đến Server (Singleton).
 * Chịu trách nhiệm duy trì một kết nối TCP duy nhất,
 * gửi dữ liệu đi và nhận dữ liệu về.
 */
public class SocketManager {
    private static SocketManager instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    private Consumer<String> onMessageReceived;
    // Cấu hình server
    private final String SERVER_HOST = AppConfig.getServerHost();
    private final int SERVER_PORT = AppConfig.getServerPort();


    private SocketManager() {}  // private constructor -> Singleton

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    /**
     * Kết nối đến server. Nếu đã kết nối rồi thì không làm gì.
     * @throws IOException nếu kết nối thất bại (server chưa chạy, sai cổng...)
     */
    public void connect() throws IOException {
        if (!connected) {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("Đã kết nối đến server tại " + SERVER_HOST + ":" + SERVER_PORT);
        }
    }

    /**
     * Gửi một dòng dữ liệu đến server.
     * @param message chuỗi cần gửi (theo giao thức đã thống nhất)
     * @throws IllegalStateException nếu chưa kết nối
     */
    public void send(String message) {
        if (out != null) {
            out.println(message);
        } else {
            throw new IllegalStateException("Chưa kết nối đến server. Hãy gọi connect() trước.");
        }
    }

    /**
     * Nhận một dòng dữ liệu từ server
     * @return chuỗi nhận được (có thể null nối tiếp nối)
     * @throws IOException nếu có lỗi đọc
     */
    public String receive() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        throw new IllegalStateException("Chưa kết nối đến server.");
    }

    /**
     * Đóng kết nối, giải phóng tài nguyên.
     */
    public void disconnect() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            connected = false;
            System.out.println("Đã ngắt kết nối server.");
        }
    }
    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }
    public boolean isConnected() {
        return connected;
    }
}
