package com.example.fooddelivery.Dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDate;
import com.example.fooddelivery.Model.User;
import com.example.fooddelivery.Database.DatabaseConnector;

public class UserDAO {
    private DatabaseConnector dbConnector;

    public UserDAO(DatabaseConnector dbConnector) {
        this.dbConnector = dbConnector;
    }

    public static ObservableList<User> getAllUsers() {
        ObservableList<User> userList = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null) {
                showAlert("Lỗi kết nối", "Không thể kết nối tới CSDL.");
                return userList;
            }

            String query = "SELECT * FROM users";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("user_id");
                String name = rs.getString("full_name");
                String email = rs.getString("email");
                String pass = rs.getString("password_hash");
                Date sqlDate = rs.getDate("date_of_birth");
                LocalDate dob = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                String phone = rs.getString("phone_number");
                String gender = rs.getString("gender");
                String profile = rs.getString("profile_picture_url");
                String role = rs.getString("role");

                User user = new User(id, name, email, pass, dob, phone, gender, profile, role);
                userList.add(user);
            }

        } catch (SQLException e) {
            showAlert("Lỗi truy vấn", "Không thể truy vấn dữ liệu người dùng:\n" + e.getMessage());
        }

        return userList;
    }

    public static boolean updateUserRole(int userId, String newRole) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        System.out.println("DEBUG: Updating role for user " + userId + " to " + newRole);
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi cập nhật vai trò cho user ID " + userId + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Xóa một người dùng khỏi cơ sở dữ liệu dựa vào ID.
     * @param userId ID của người dùng cần xóa.
     * @return true nếu xóa thành công (ít nhất 1 dòng bị ảnh hưởng), false nếu không.
     * @throws SQLException Nếu có lỗi SQL xảy ra.
     */
    public static boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        System.out.println("INFO: Attempting to delete user with ID: " + userId); // Log hành động

        try (Connection conn = DatabaseConnector.connectDB(); // Lấy kết nối
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                throw new SQLException("Không thể kết nối tới CSDL để xóa người dùng.");
            }

            pstmt.setInt(1, userId); // Đặt tham số ID
            int affectedRows = pstmt.executeUpdate(); // Thực thi DELETE

            if (affectedRows > 0) {
                System.out.println("INFO: Successfully deleted user with ID: " + userId);
                return true;
            } else {
                System.out.println("WARN: No user found with ID: " + userId + " to delete.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi xóa user ID " + userId + ": " + e.getMessage());
            // Kiểm tra mã lỗi ràng buộc khóa ngoại (ví dụ: MySQL error code 1451)
            if (e.getErrorCode() == 1451) {
                System.err.println("WARN: Không thể xóa người dùng do có ràng buộc dữ liệu liên quan (ví dụ: đơn hàng, đánh giá).");
            }
            throw e;
        }
    }

    private static void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
