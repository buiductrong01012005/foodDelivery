package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Main;
import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Xử lý khi người dùng nhấn nút "Login".
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = usernameField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ email và mật khẩu.");
            return;
        }

        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null) {
                showAlert("Lỗi kết nối", "Không thể kết nối tới CSDL.");
                return;
            }

            String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password); // ❗ Nên hash nếu bạn đã dùng bcrypt hoặc SHA-256

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                String emailForAdmin = rs.getString("email");
                String userRole = rs.getString("role");
                User loggedInAdmin = new User();
                loggedInAdmin.setUser_id(userId);
                loggedInAdmin.setFull_name(fullName);
                loggedInAdmin.setEmail(emailForAdmin);
                loggedInAdmin.setRole(userRole);
                showAlert("Đăng nhập thành công", "Chào mừng, " + fullName + "!");
                if ("Customer".equals(userRole)) {
                    // TODO: Chuyển đến trang chính hoặc dashboard
                    openHomePage(); // 👉 Mở giao diện UserHome.fxml
                } else if ("Admin".equals(userRole)) {
                    Main.showAdminContainerView(loggedInAdmin);
                }


            } else {
                showAlert("Đăng nhập thất bại", "Email hoặc mật khẩu không đúng.");
            }

        } catch (SQLException e) {
            showAlert("Lỗi SQL", "Không thể thực hiện kiểm tra: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Xử lý khi người dùng nhấn vào "Forget your password?".
     */
    @FXML
    private void ForgetPass(MouseEvent event) {
        Main.showForgetPasswordView();
    }

    /**
     * Xử lý khi người dùng nhấn nút "Register".
     */
    @FXML
    private void goToRegister(ActionEvent event) {
        Main.showRegisterView(); // chuyển sang Register.fxml
    }

    /**
     * Hiển thị thông báo popup.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Mở giao diện chính sau khi đăng nhập thành công.
     */
    private void openHomePage() {
        try {
            // ⚠️ Đường dẫn này phải đúng vị trí của UserHome.fxml trong resources
            // Ví dụ nếu nằm trong resources/view/UserHome.fxml => "/view/UserHome.fxml"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User/UserHome.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Trang chủ - ShopeeFood");
            stage.show();
        } catch (IOException e) {
            showAlert("Lỗi giao diện", "Không thể mở giao diện trang chủ.");
            e.printStackTrace();
        }
    }
}
