package com.auction.team3HxD.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Đọc cấu hình từ file application.properties.
 * Cung cấp các hằng số cấu hình cho toàn bộ client.
 */
public class AppConfig {
    private static final Properties props = new Properties();
    private static boolean mockEnabled = true;
    private static String serverHost = "localhost";
    private static int serverPort = 12345;

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                mockEnabled = Boolean.parseBoolean(props.getProperty("app.mock.enabled", "true"));
                serverHost = props.getProperty("server.host", "localhost");
                serverPort = Integer.parseInt(props.getProperty("server.port", "12345"));
            } else {
                System.err.println("File application.properties not found, using default values.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Giữ nguyên giá trị mặc định
        }
    }

    public static boolean isMockEnabled() {
        return mockEnabled;
    }

    public static String getServerHost() {
        return serverHost;
    }

    public static int getServerPort() {
        return serverPort;
    }
}