package com.example.fooddelivery.Dao;

import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.Food;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FoodDao {

    public static List<Food> getAllFoods() {
        List<Food> foodList = new ArrayList<>();

        String sql = "SELECT * FROM foods WHERE availability_status = 'available'";

        try (Connection conn = DatabaseConnector.connectDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Food food = new Food(
                        rs.getInt("food_id"),
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("availability_status"),
                        rs.getString("image_url"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                foodList.add(food);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return foodList;
    }
}
