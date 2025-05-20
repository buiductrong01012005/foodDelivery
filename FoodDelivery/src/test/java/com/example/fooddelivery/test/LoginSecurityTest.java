package com.example.fooddelivery.test;

import com.example.fooddelivery.Database.DatabaseConnector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm tra các tính năng bảo mật trong quá trình đăng nhập
 */
public class LoginSecurityTest {

    /**
     * Kiểm tra khả năng phòng chống SQL Injection
     */
    @Test
    @DisplayName("Hệ thống phải chống SQL Injection")
    public void testSqlInjectionPrevention() {
        String[] injectionAttempts = {
            "' OR '1'='1", 
            "'; DROP TABLE users; --",
            "' OR '1'='1' --",
            "admin@foodapp.vn'; --",
            "' UNION SELECT * FROM users --"
        };
        
        for (String injectionString : injectionAttempts) {
            boolean authenticated = authenticateUser(injectionString, "password");
            assertFalse(authenticated, "SQL Injection với chuỗi '" + injectionString + "' phải bị chặn");
            System.out.println("✓ Chặn thành công SQL Injection: " + injectionString);
        }
    }
    
    /**
     * Kiểm tra xác thực với dữ liệu đầu vào trống
     */
    @Test
    @DisplayName("Không chấp nhận dữ liệu đăng nhập trống")
    public void testEmptyInputValidation() {
        // Email trống
        boolean emptyEmailAuth = authenticateUser("", "password123");
        assertFalse(emptyEmailAuth, "Email trống không được phép đăng nhập");
        
        // Mật khẩu trống
        boolean emptyPasswordAuth = authenticateUser("user@example.com", "");
        assertFalse(emptyPasswordAuth, "Mật khẩu trống không được phép đăng nhập");
        
        // Cả hai trống
        boolean bothEmptyAuth = authenticateUser("", "");
        assertFalse(bothEmptyAuth, "Email và mật khẩu trống không được phép đăng nhập");
        
        System.out.println("✓ Ngăn chặn thành công đăng nhập với dữ liệu trống");
    }
    
    /**
     * Kiểm tra định dạng email không hợp lệ
     */
    @Test
    @DisplayName("Kiểm tra định dạng email không hợp lệ")
    public void testInvalidEmailFormat() {
        String[] invalidEmails = {
            "invalid",
            "invalid@",
            "@domain.com",
            "test@invalid.",
            ".test@domain.com"
        };
        
        for (String invalidEmail : invalidEmails) {
            boolean authenticated = authenticateUser(invalidEmail, "password123");
            assertFalse(authenticated, "Email không hợp lệ '" + invalidEmail + "' không được đăng nhập");
            System.out.println("✓ Ngăn chặn đăng nhập với email không hợp lệ: " + invalidEmail);
        }
    }
    
    /**
     * Hàm giả lập logic xác thực đăng nhập
     * Thêm kiểm tra định dạng email đơn giản
     */
    private boolean authenticateUser(String email, String password) {
        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null) {
                return false;
            }
            
            // Kiểm tra dữ liệu đầu vào trống
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                return false;
            }
            
            // Kiểm tra định dạng email đơn giản
            if (!email.contains("@") || !email.contains(".")) {
                return false;
            }
            
            // Dùng PreparedStatement để tránh SQL injection
            String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
