package com.auction.team3hxd.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {

    private static String URL = "jdbc:mysql://localhost:3306/auction_db";
    private static String USER = "root";
    private static String PASS = "admin";
    static {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);

            URL = prop.getProperty("db.url", URL);
            USER = prop.getProperty("db.user", USER);
            PASS = prop.getProperty("db.password", PASS);

            System.out.println(">>> [DATABASE] Đã nạp cấu hình từ config.properties");
        } catch (Exception ex) {
            System.out.println(">>> [DATABASE] CẢNH BÁO: Không tìm thấy file config.properties, đang dùng cấu hình mặc định (root/admin)...");
        }
    }
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi kết nối DB", e);
        }
    }
}