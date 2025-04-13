package com.example.fooddelivery.Database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnector {
    public static Connection connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // nhớ đổi mk
            Connection connect = DriverManager.getConnection("jdbc:mysql://localhost/fooddelivery", "root", "mk workbench");
            return connect;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
