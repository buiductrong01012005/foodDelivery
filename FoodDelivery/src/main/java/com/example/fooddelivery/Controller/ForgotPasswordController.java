package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Main; // Để chuyển màn hình
import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Service.PasswordResetOtpService;

// Import các Exceptions (nếu bạn dùng phiên bản Service ném Exception)
// import com.example.fooddelivery.Service.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField; // Sử dụng PasswordField
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox; // Import VBox để điều khiển visibility/managed

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ForgotPasswordController implements Initializable {

    // --- Khai báo khớp với fx:id trong FXML ---
    @FXML private VBox resetPane;
    @FXML private TextField emailField;
    @FXML private Button resetClick; // Nút Send OTP

    @FXML private VBox otpPane;
    @FXML private TextField otpField;
    @FXML private PasswordField newPasswordField; // Đã là PasswordField
    @FXML private PasswordField confirmPasswordField; // Đã là PasswordField
    @FXML private Button submitResetButton; // Nút Submit New Password

    @FXML private Button backButton;
    @FXML private Button registerButton;

    // --- Service ---
    private PasswordResetOtpService passwordResetService;
    private DatabaseConnector dbConnector;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.dbConnector = new DatabaseConnector();
        this.passwordResetService = new PasswordResetOtpService(dbConnector);

        switchPanes(false);
    }

    /**
     * Xử lý sự kiện cho nút "Send OTP" (resetClick).
     */
    @FXML
    private void handleSendOtpAction(ActionEvent event) {
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập địa chỉ email hợp lệ.");
            return;
        }

        resetClick.setDisable(true);
        showAlert(Alert.AlertType.INFORMATION, "Đang xử lý...", "Đang gửi mã OTP, vui lòng đợi...", false); // Non-blocking

        new Thread(() -> {
            boolean requestProcessed = false;
            String errorMessage = "Đã xảy ra lỗi khi gửi OTP.";

            try {
                requestProcessed = passwordResetService.requestPasswordResetOtp(email);
            } catch (SQLException e) {
                errorMessage = "Lỗi kết nối cơ sở dữ liệu. Vui lòng thử lại sau.";
                System.err.println("ERROR: Lỗi SQL khi yêu cầu OTP: " + e.getMessage());
                e.printStackTrace();
                requestProcessed = false;
            } catch (Exception e) { // Bao gồm cả EmailSendingException nếu service throw
                errorMessage = "Lỗi khi gửi OTP. Vui lòng thử lại.";
                System.err.println("ERROR: Lỗi khi yêu cầu OTP: " + e.getMessage());
                e.printStackTrace();
                requestProcessed = false;
            }

            final boolean finalSuccess = requestProcessed;
            final String finalErrorMessage = errorMessage;

            Platform.runLater(() -> {
                resetClick.setDisable(false);
                if (finalSuccess) {
                    showAlert(Alert.AlertType.INFORMATION, "Kiểm tra Email", "Nếu email của bạn tồn tại, mã OTP đã được gửi. Vui lòng kiểm tra hộp thư (kể cả Spam).");
                    switchPanes(true); // Hiện pane OTP
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", finalErrorMessage);
                }
            });
        }).start();
    }

    /**
     * Xử lý sự kiện cho nút "Submit New Password" (submitResetButton).
     */
    @FXML
    private void handleSubmitResetAction(ActionEvent event) {
        String otp = otpField.getText();
        String newPassword = newPasswordField.getText(); // Lấy text từ PasswordField
        String confirmPassword = confirmPasswordField.getText(); // Lấy text từ PasswordField

        // --- Input Validation ---
        final int expectedOtpLength = PasswordResetOtpService.OTP_LENGTH; // Lấy độ dài OTP từ Service (nếu đã làm public) hoặc hardcode 6
        if (otp == null || !otp.matches("\\d{" + expectedOtpLength + "}")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập mã OTP gồm " + expectedOtpLength + " chữ số.");
            return;
        }
        if (newPassword == null || newPassword.length() < 6) { // Kiểm tra độ dài tối thiểu
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp.");
            return;
        }
        // --- End Validation ---

        submitResetButton.setDisable(true);
        showAlert(Alert.AlertType.INFORMATION, "Đang xử lý...", "Đang đặt lại mật khẩu...", false);

        // Thực hiện trong luồng nền
        new Thread(() -> {
            boolean success = false;
            String resultMessage = "Đã xảy ra lỗi không mong muốn khi đặt lại mật khẩu.";
            Alert.AlertType resultType = Alert.AlertType.ERROR;

            try {
                success = passwordResetService.resetPasswordWithOtp(otp, newPassword);
                if (success) {
                    resultMessage = "Đặt lại mật khẩu thành công! Vui lòng quay lại và đăng nhập.";
                    resultType = Alert.AlertType.INFORMATION;
                } else {
                    resultMessage = "Lỗi: Mã OTP không đúng, hết hạn hoặc có lỗi khi cập nhật. Vui lòng thử lại.";
                }

            } catch (SQLException e) {
                resultMessage = "Lỗi: Hệ thống đang gặp sự cố khi cập nhật. Vui lòng thử lại sau.";
                resultType = Alert.AlertType.ERROR;
                System.err.println("ERROR: Lỗi cơ sở dữ liệu khi reset mật khẩu: " + e.getMessage());
                e.printStackTrace();
            } catch (IllegalArgumentException e) { // Bắt lỗi mật khẩu yếu từ service nếu có
                resultMessage = "Lỗi: " + e.getMessage();
                resultType = Alert.AlertType.ERROR;
            } catch (Exception e) {
                resultMessage = "Lỗi hệ thống.";
                resultType = Alert.AlertType.ERROR;
                System.err.println("ERROR: Lỗi không xác định khi reset mật khẩu: " + e.getMessage());
                e.printStackTrace();
            }

            // Cập nhật UI
            final String finalMessage = resultMessage;
            final Alert.AlertType finalAlertType = resultType;
            final boolean finalSuccess = success;
            Platform.runLater(() -> {
                submitResetButton.setDisable(false);
                showAlert(finalAlertType, (finalSuccess ? "Thành công" : "Thất bại"), finalMessage);
                if (finalSuccess) {
                    handleBackAction(null); // Tự động quay lại Login khi thành công
                }
            });

        }).start();
    }

    /**
     * Xử lý sự kiện cho nút Back (backButton).
     */
    @FXML
    private void handleBackAction(ActionEvent event) {
        Main.showLoginView(); // Gọi phương thức chuyển về màn hình Login
    }

    /**
     * Xử lý sự kiện cho nút Register (registerButton).
     */
    @FXML
    private void goToRegister(ActionEvent event) {
        Main.showRegisterView(); // Gọi phương thức chuyển sang màn hình Register
    }

    /**
     * Hiển thị thông báo Alert.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        showAlert(alertType, title, content, true); // Mặc định blocking
    }

    private void showAlert(Alert.AlertType alertType, String title, String content, boolean wait) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showAlertInternal(alertType, title, content, wait));
        } else {
            showAlertInternal(alertType, title, content, wait);
        }
    }

    private void showAlertInternal(Alert.AlertType alertType, String title, String content, boolean wait) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (wait) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    /**
     * Chuyển đổi giữa pane nhập email và pane nhập OTP.
     */
    private void switchPanes(boolean showOtpPane) {
        resetPane.setVisible(!showOtpPane);
        resetPane.setManaged(!showOtpPane);
        otpPane.setVisible(showOtpPane);
        otpPane.setManaged(showOtpPane);

        if (showOtpPane) {
            otpField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            otpField.requestFocus();
        } else {
            emailField.requestFocus();
        }
    }
}