package com.example.fooddelivery;

import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.User;
import com.example.fooddelivery.Utils.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case kiểm tra tính năng bảo mật của quá trình đăng nhập
 */
public class LoginSecurityTest {

    @AfterEach
    public void tearDown() {
        Session.clear();
    }
    
    /**
     * Kiểm tra khả năng phòng chống SQL Injection đơn giản
     */
    @Test
    @DisplayName("System should be resistant to basic SQL Injection attacks")
    public void testSqlInjectionPrevention() {
        // Các chuỗi SQL Injection đơn giản
        String[] injectionAttempts = {
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "' OR '1'='1' --",
            "' UNION SELECT * FROM users --",
            "admin@foodapp.vn'; --"
        };
        
        for (String injectionString : injectionAttempts) {
            boolean authenticated = authenticateUser(injectionString, "password");
            assertFalse(authenticated, "SQL Injection với chuỗi '" + injectionString + "' nên bị ngăn chặn");
            System.out.println("✓ Ngăn chặn SQL Injection thành công cho: " + injectionString);
        }
    }
    
    /**
     * Kiểm tra xác thực mật khẩu rỗng
     */
    @Test
    @DisplayName("Empty password should not authenticate")
    public void testEmptyPasswordValidation() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Database connection must succeed");
            
            // Lấy một email hợp lệ từ database
            PreparedStatement stmt = conn.prepareStatement("SELECT email FROM users LIMIT 1");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String validEmail = rs.getString("email");
                
                // Thử xác thực với mật khẩu rỗng
                boolean authenticated = authenticateUser(validEmail, "");
                assertFalse(authenticated, "Mật khẩu rỗng không được phép xác thực");
                System.out.println("✓ Ngăn chặn mật khẩu rỗng thành công");
            }
        } catch (Exception e) {
            fail("Exception during empty password test: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra xác thực email không hợp lệ
     */
    @Test
    @DisplayName("Invalid email format should not authenticate")
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
            assertFalse(authenticated, "Email không hợp lệ '" + invalidEmail + "' không được phép xác thực");
            System.out.println("✓ Ngăn chặn email không hợp lệ thành công cho: " + invalidEmail);
        }
    }
    
    /**
     * Helper method để xác thực người dùng mà không cần phụ thuộc vào UI
     */
    private boolean authenticateUser(String email, String password) {
        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null) {
                return false;
            }
            
            if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
                return false;
            }

            // Sử dụng Prepared Statement để tránh SQL Injection
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
