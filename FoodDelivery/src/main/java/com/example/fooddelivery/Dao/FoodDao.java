package com.example.fooddelivery.Dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDateTime; // Import LocalDateTime

import com.example.fooddelivery.Model.Food;
import com.example.fooddelivery.Database.DatabaseConnector;

public class FoodDAO {
    // Bỏ constructor nếu không cần
    // private DatabaseConnector dbConnector;
    // public FoodDAO(DatabaseConnector dbConnector) { this.dbConnector = dbConnector; }

    /**
     * Lấy danh sách TẤT CẢ món ăn cùng với tên loại món ăn.
     * @return ObservableList chứa các đối tượng Food đã bao gồm categoryName.
     * @throws SQLException Nếu có lỗi truy vấn CSDL.
     */
    public static ObservableList<Food> getAllFoods() throws SQLException { // Thêm throws SQLException
        ObservableList<Food> foodList = FXCollections.observableArrayList();

        // --- SỬA SQL ĐỂ JOIN VÀ LẤY TÊN LOẠI ---
        // Chọn các cột từ foods (f) và cột name từ food_categories (fc)
        // Đặt alias (AS) cho fc.name để tránh trùng tên với f.name
        String query = "SELECT f.*, fc.name AS category_name " +
                "FROM foods f " +
                "LEFT JOIN food_categories fc ON f.category_id = fc.category_id";

        System.out.println("DEBUG: Executing query: " + query);

        try (Connection conn = DatabaseConnector.connectDB(); // Dùng static method
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (conn == null) {
                throw new SQLException("Không thể kết nối tới CSDL.");
            }

            System.out.println("DEBUG: Connection successful. Processing food result set...");

            while (rs.next()) {
                try {
                    // Lấy thông tin từ bảng foods
                    int id = rs.getInt("food_id");
                    int categoryId = rs.getInt("category_id"); // Vẫn lấy categoryId
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    double price = rs.getDouble("price");
                    String availabilityStatus = rs.getString("availability_status");
                    String imageUrl = rs.getString("image_url");
                    int createdBy = rs.getInt("created_by");
                    int updatedBy = rs.getInt("updated_by");
                    // Lấy Timestamp và chuyển sang LocalDateTime (xử lý null)
                    Timestamp createdAtTS = rs.getTimestamp("created_at");
                    LocalDateTime createdAt = (createdAtTS != null) ? createdAtTS.toLocalDateTime() : null;
                    Timestamp updatedAtTS = rs.getTimestamp("updated_at");
                    LocalDateTime updatedAt = (updatedAtTS != null) ? updatedAtTS.toLocalDateTime() : null;

                    // <<< LẤY TÊN LOẠI TỪ KẾT QUẢ JOIN >>>
                    String categoryName = rs.getString("category_name"); // Lấy theo alias 'category_name'

                    // <<< SỬ DỤNG CONSTRUCTOR MỚI CỦA FOOD >>>
                    Food food = new Food(id, categoryId, categoryName, name, description, price,
                            availabilityStatus, imageUrl, createdBy, updatedBy,
                            createdAt, updatedAt);
                    foodList.add(food);
                    // System.out.println("DEBUG: Added food: " + food.toString());

                } catch (Exception e) {
                    System.err.println("ERROR: Lỗi khi xử lý hàng dữ liệu food: " + e.getMessage());
                    e.printStackTrace(); // In chi tiết lỗi của hàng đó
                }
            }
            System.out.println("DEBUG: Finished processing food result set. Found " + foodList.size() + " foods.");

        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi thực thi getAllFoods: " + e.getMessage());
            throw e; // Ném lại lỗi
        }

        return foodList;
    }

    /**
     * Xóa món ăn theo ID.
     * !!! Lỗi trong code gốc: đang xóa từ bảng 'users' thay vì 'foods' !!!
     * @param foodId ID món ăn cần xóa.
     * @return true nếu xóa thành công.
     * @throws SQLException Nếu lỗi SQL.
     */
    public static boolean deleteFood(int foodId) throws SQLException {
        // <<< SỬA LẠI TÊN BẢNG THÀNH 'foods' >>>
        String sql = "DELETE FROM foods WHERE food_id = ?";
        System.out.println("INFO: Attempting to delete food with ID: " + foodId);

        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                throw new SQLException("Không thể kết nối tới CSDL để xóa món ăn.");
            }

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
            if (e.getErrorCode() == 1451) { // MySQL FK constraint
                System.err.println("WARN: Không thể xóa món ăn do có ràng buộc dữ liệu liên quan (đơn hàng, đánh giá).");
            }
            throw e;
        }
    }

    // Bỏ hàm showAlert trong DAO
    /*
    private static void showAlert(String title, String message) { ... }
    */

    private static void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
