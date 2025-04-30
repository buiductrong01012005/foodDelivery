package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Database.DatabaseConnector;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField cfPasswordField;

    /**
     * Xử lý khi người dùng nhấn nút "Submit".
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String fullName = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = cfPasswordField.getText();

        if (email.isEmpty() || phone.isEmpty() || fullName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng điền đầy đủ tất cả các trường.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Lỗi xác nhận mật khẩu", "Mật khẩu xác nhận không khớp.");
            return;
        }

        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null) {
                showAlert("Lỗi kết nối", "Không thể kết nối đến cơ sở dữ liệu.");
                return;
            }

            String insertSql = "INSERT INTO users (full_name, email, password_hash, phone_number, role) VALUES (?, ?, ?, ?, 'Customer')";
            PreparedStatement pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, password); // ❗ Gợi ý: dùng hash mật khẩu sau
            pstmt.setString(4, phone);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                showAlert("Thành công", "Tạo tài khoản thành công!");
                goToLogin(event);
            } else {
                showAlert("Thất bại", "Không thể tạo tài khoản. Vui lòng thử lại.");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                showAlert("Tài khoản đã tồn tại", "Email hoặc số điện thoại đã được đăng ký.");
            } else {
                showAlert("Lỗi SQL", "Chi tiết: " + e.getMessage());
            }
            e.printStackTrace();
        }
    }

    /**
     * Xử lý khi người dùng nhấn nút "⬅" để quay lại Login.fxml.
     */
    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene loginScene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Lỗi", "Không thể chuyển về trang đăng nhập: " + e.getMessage());
        }
    }

    /**
     * Hiển thị hộp thoại thông báo.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
