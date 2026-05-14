package com.auction.team3HxD.util;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketService {
    private static SocketService instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;
    private Consumer<String> messageHandler;
    private Thread listenerThread;

    private SocketService() {}

    public static synchronized SocketService getInstance() {
        if (instance == null) {
            instance = new SocketService();
        }
        return instance;
    }

    /**
     * Kết nối đến server. Nếu đã kết nối rồi thì không làm gì.
     * @param host địa chỉ server (localhost)
     * @param port cổng server (5000)
     */
    public void connect(String host, int port) throws IOException {
        if (socket != null && !socket.isClosed()) {
            return; // Đã kết nối, không tạo lại
        }
        running = true;
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        startListening();
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            while (running) {
                try {
                    String msg = in.readLine();
                    System.out.println(">>> SocketService vừa đọc được: " + msg);
                    if (msg != null && messageHandler != null) {
                        // Đảm bảo cập nhật giao diện trên JavaFX Thread
                        javafx.application.Platform.runLater(() -> messageHandler.accept(msg));
                    }
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                    break; // Mất kết nối, thoát vòng lặp
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Gửi một dòng lệnh đến server.
     */
    public void send(String message) {
        if (out != null) {
            out.println(message);
        } else {
            throw new IllegalStateException("Socket chưa được kết nối. Hãy gọi connect() trước.");
        }
    }

    /**
     * Đăng ký handler để nhận dữ liệu từ server.
     * Mỗi controller sẽ gọi hàm này trong initialize().
     */
    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    /**
     * Đóng socket và dừng luồng lắng nghe.
     */
    public void shutdown() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}
