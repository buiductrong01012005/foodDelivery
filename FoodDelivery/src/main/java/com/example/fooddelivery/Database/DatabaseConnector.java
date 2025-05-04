package com.example.fooddelivery.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    private static final String URL = "jdbc:mysql://localhost/simple_food_delivery_db";
    private static final String USER = "root";
    private static final String PASSWORD = "hung2k5hht"; // bạn nhớ đổi nếu cần

    public static Connection connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // bắt buộc khi dùng MySQL với JDBC
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Driver MySQL JDBC.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Kết nối tới CSDL thất bại: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
