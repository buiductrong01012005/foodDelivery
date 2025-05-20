package com.example.fooddelivery.Dao;

import com.example.fooddelivery.Database.DatabaseConnector; // Assuming this class exists
import com.example.fooddelivery.Model.Food;
import com.example.fooddelivery.Model.ReviewDisplay;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class FoodDAO {

    public static ObservableList<Food> getAllFoods() throws SQLException {
        ObservableList<Food> foodList = FXCollections.observableArrayList();
        String query = "SELECT f.*, fc.name AS category_name " +
                "FROM foods f " +
                "LEFT JOIN food_categories fc ON f.category_id = fc.category_id";
        System.out.println("DEBUG: Executing query: " + query);
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (conn == null) throw new SQLException("Không thể kết nối tới CSDL.");
            System.out.println("DEBUG: Connection successful. Processing food result set...");
            while (rs.next()) {
                try {
                    int id = rs.getInt("food_id");
                    int categoryId = rs.getInt("category_id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    double price = rs.getDouble("price");
                    String availabilityStatus = rs.getString("availability_status");
                    String imageUrl = rs.getString("image_url");
                    int createdBy = rs.getInt("created_by");
                    int updatedBy = rs.getInt("updated_by");
                    Timestamp createdAtTS = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = (createdAtTS != null) ? createdAtTS.toLocalDateTime() : null;
                    Timestamp updatedAtTS = rs.getTimestamp("updated_at");
                    LocalDateTime updatedAt = (updatedAtTS != null) ? updatedAtTS.toLocalDateTime() : null;
                    String categoryName = rs.getString("category_name");
                    Food food = new Food(id, categoryId, categoryName, name, description, price,
                            availabilityStatus, imageUrl, createdBy, updatedBy, createdAt, updatedAt);
                    foodList.add(food);
                } catch (Exception e) {
                    System.err.println("ERROR: Lỗi khi xử lý hàng dữ liệu food: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("DEBUG: Finished processing food result set. Found " + foodList.size() + " foods.");
        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi thực thi getAllFoods: " + e.getMessage());
            throw e;
        }
        return foodList;
    }

    public static boolean deleteFood(int foodId) throws SQLException {
        String sql = "DELETE FROM foods WHERE food_id = ?";
        System.out.println("INFO: Attempting to delete food with ID: " + foodId);
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Không thể kết nối tới CSDL để xóa món ăn.");
            pstmt.setInt(1, foodId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("INFO: Successfully deleted food with ID: " + foodId);
                return true;
            } else {
                System.out.println("WARN: No food found with ID: " + foodId + " to delete.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi xóa food ID " + foodId + ": " + e.getMessage());
            if (e.getErrorCode() == 1451) {
                System.err.println("WARN: Không thể xóa món ăn do có ràng buộc dữ liệu liên quan.");
            }
            throw e;
        }
    }

    public static Food addFood(Food food) throws SQLException {
        String sql = "INSERT INTO foods (category_id, name, description, price, availability_status, image_url, created_by, updated_by, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())"; // DB sets created_at, updated_at
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;
        System.out.println("INFO: DAO Attempting to add new food: " + food.getName());
        try {
            conn = DatabaseConnector.connectDB();
            if (conn == null) throw new SQLException("Cannot connect to DB to add food.");
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, food.getCategory_id());
            pstmt.setString(2, food.getName());
            pstmt.setString(3, food.getDescription());
            pstmt.setDouble(4, food.getPrice());
            pstmt.setString(5, food.getAvailability_status());
            pstmt.setString(6, food.getImage_url());
            pstmt.setInt(7, food.getCreated_by());
            pstmt.setInt(8, food.getUpdated_by());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating food failed, no rows affected.");

            generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                food.setFood_id(generatedKeys.getInt(1));
                // Re-fetch or set timestamps if needed, though DB handles it here
                System.out.println("INFO: DAO Successfully added food with ID: " + food.getFood_id());
                return food;
            } else {
                throw new SQLException("Creating food failed, no ID obtained.");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: DAO SQL Error adding food: " + e.getMessage());
            throw e;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }

    public static boolean updateFood(Food food) throws SQLException {
        String sql = "UPDATE foods SET category_id = ?, name = ?, description = ?, price = ?, " +
                "availability_status = ?, image_url = ?, updated_by = ?, updated_at = NOW() " +
                "WHERE food_id = ?";
        System.out.println("INFO: DAO Attempting to update food ID: " + food.getFood_id());
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Cannot connect to DB to update food.");
            pstmt.setInt(1, food.getCategory_id());
            pstmt.setString(2, food.getName());
            pstmt.setString(3, food.getDescription());
            pstmt.setDouble(4, food.getPrice());
            pstmt.setString(5, food.getAvailability_status());
            pstmt.setString(6, food.getImage_url());
            pstmt.setInt(7, food.getUpdated_by());
            pstmt.setInt(8, food.getFood_id());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("INFO: DAO Successfully updated food ID: " + food.getFood_id());
                return true;
            } else {
                System.out.println("WARN: DAO No food found with ID: " + food.getFood_id() + " to update, or no changes made.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: DAO SQL Error updating food ID " + food.getFood_id() + ": " + e.getMessage());
            throw e;
        }
    }

    public static ObservableList<String> getAllCategoryNames() throws SQLException {
        ObservableList<String> categoryNames = FXCollections.observableArrayList();
        String sql = "SELECT name FROM food_categories ORDER BY name";
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categoryNames.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching category names: " + e.getMessage());
            throw e;
        }
        return categoryNames;
    }

    public static int getCategoryIdByName(String categoryName) throws SQLException {
        String sql = "SELECT category_id FROM food_categories WHERE name = ?";
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching category ID by name '" + categoryName + "': " + e.getMessage());
            // Consider throwing exception or returning a more indicative error value like OptionalInt.empty()
        }
        return -1; // Indicates not found or error
    }

    public static ObservableList<ReviewDisplay> getReviewsForFood(int foodId) throws SQLException {
        ObservableList<ReviewDisplay> reviews = FXCollections.observableArrayList();
        String sql = "SELECT fr.food_review_id, fr.user_id, u.full_name, u.email, u.phone_number, fr.comment, fr.rating, fr.created_at AS review_date, fr.status " +
                "FROM food_reviews fr " +
                "JOIN users u ON fr.user_id = u.user_id " +
                "WHERE fr.food_id = ? " +
                "ORDER BY fr.created_at DESC";
        System.out.println("INFO: DAO Fetching reviews for food ID: " + foodId);
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Cannot connect to DB to fetch reviews.");
            pstmt.setInt(1, foodId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int reviewId = rs.getInt("food_review_id");
                    int userId = rs.getInt("user_id");
                    String userName = rs.getString("full_name");
                    String userEmail = rs.getString("email");
                    String userPhoneNumber = rs.getString("phone_number");
                    String comment = rs.getString("comment");
                    int rating = rs.getInt("rating");
                    Timestamp reviewDateTS = rs.getTimestamp("review_date");
                    LocalDateTime reviewDate = (reviewDateTS != null) ? reviewDateTS.toLocalDateTime() : null;
                    String reviewStatus = rs.getString("status");
                    reviews.add(new ReviewDisplay(reviewId, userId, userName, userEmail, userPhoneNumber, comment, rating, reviewDate, reviewStatus));
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: DAO SQL Error fetching reviews for food ID " + foodId + ": " + e.getMessage());
            throw e;
        }
        System.out.println("INFO: DAO Found " + reviews.size() + " reviews for food ID: " + foodId);
        return reviews;
    }

    public static boolean updateReviewStatus(int reviewId, String newStatus) throws SQLException {
        String sql = "UPDATE food_reviews SET status = ? WHERE food_review_id = ?";
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, reviewId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}