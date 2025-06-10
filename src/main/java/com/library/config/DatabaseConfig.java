package com.library.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Lớp cấu hình cơ sở dữ liệu để thiết lập kết nối
public class DatabaseConfig {
    // URL kết nối đến cơ sở dữ liệu MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/library?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    // Tên người dùng để kết nối cơ sở dữ liệu
    private static final String USER = "root";
    // Mật khẩu người dùng để kết nối cơ sở dữ liệu (cần thay đổi bằng mật khẩu thực tế của bạn)
    private static final String PASSWORD = "Lapmo843@"; // Thay bằng mật khẩu MySQL của bạn

    // Khối static: Được thực thi một lần khi lớp được tải, dùng để đăng ký driver JDBC
    static {
        try {
            // Đăng ký driver MySQL để JVM có thể tìm thấy và sử dụng nó
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Ném RuntimeException nếu không thể tải driver (lỗi nghiêm trọng)
            throw new RuntimeException("Không thể tải driver MySQL: " + e.getMessage());
        }
    }

    // Phương thức tĩnh để lấy một đối tượng Connection đến cơ sở dữ liệu
    public static Connection getConnection() throws SQLException {
        // Trả về một đối tượng Connection bằng cách sử dụng DriverManager
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
