package com.example.fooddelivery.Dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.fooddelivery.Model.Food;
import com.example.fooddelivery.Database.DatabaseConnector;

public class FoodDAO {
    private DatabaseConnector dbConnector;

    public FoodDAO(DatabaseConnector dbConnector) {
        this.dbConnector = dbConnector;
    }

    public static ObservableList<Food> getAllFoods() {
        ObservableList<Food> foodList = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null) {
                showAlert("Lỗi kết nối", "Không thể kết nối tới CSDL.");
                return foodList;
            }

            String query = "SELECT * FROM foods";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("food_id");
                int category_id = rs.getInt("category_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String availability_status = rs.getString("availability_status");
                String image_url = rs.getString("image_url");
                int created_by = rs.getInt("created_by");
                int updated_by = rs.getInt("updated_by");
                LocalDateTime created_at = rs.getTimestamp("created_at").toLocalDateTime();
                LocalDateTime updated_at = rs.getTimestamp("updated_at").toLocalDateTime();

                Food food = new Food(id,category_id, name, description, price, availability_status, image_url, created_by, updated_by, created_at, updated_at);
                foodList.add(food);
            }

        } catch (SQLException e) {
            showAlert("Lỗi truy vấn", "Không thể truy vấn dữ liệu người dùng:\n" + e.getMessage());
        }

        return foodList;
    }

    private static void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
