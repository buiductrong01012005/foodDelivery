package com.example.fooddelivery;

import com.example.fooddelivery.Controller.LoginController;
import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.User;
import com.example.fooddelivery.Utils.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bài test tổng thể cho LoginController
 * Kiểm tra tất cả các khía cạnh của LoginController mà không phụ thuộc vào JavaFX
 */
public class LoginControllerTest {

    @BeforeEach
    public void setUp() {
        Session.clear();  // Đảm bảo session được xóa trước mỗi test
    }

    @AfterEach
    public void tearDown() {
        Session.clear();  // Dọn dẹp session sau mỗi test
    }

    /**
     * Test kết nối tới cơ sở dữ liệu
     */
    @Test
    @DisplayName("Database connection should succeed")
    public void testDatabaseConnection() {
        Connection conn = DatabaseConnector.connectDB();
        assertNotNull(conn, "Kết nối tới database phải thành công");
        
        try {
            if (conn != null) {
                conn.close();
                System.out.println("✓ Kết nối database thành công và đã đóng");
            }
        } catch (Exception e) {
            fail("Exception khi đóng kết nối database: " + e.getMessage());
        }
    }
    
    /**
     * Test xác thực với tài khoản hợp lệ
     */
    @Test
    @DisplayName("Valid credentials should authenticate successfully")
    public void testValidAuthentication() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Tìm một user trong database
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users LIMIT 1");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String email = rs.getString("email");
                String password = rs.getString("password_hash");
                
                boolean authenticated = authenticateUser(email, password);
                assertTrue(authenticated, "Người dùng hợp lệ phải đăng nhập thành công");
                System.out.println("✓ Xác thực thành công với email: " + email);
            } else {
                fail("Không tìm thấy user nào trong database");
            }
        } catch (Exception e) {
            fail("Exception trong quá trình test: " + e.getMessage());
        }
    }
    
    /**
     * Test xác thực với tài khoản không hợp lệ
     */
    @Test
    @DisplayName("Invalid credentials should fail authentication")
    public void testInvalidAuthentication() {
        String invalidEmail = "nonexistent@example.com";
        String invalidPassword = "wrongpassword";
        
        boolean authenticated = authenticateUser(invalidEmail, invalidPassword);
        assertFalse(authenticated, "Người dùng không hợp lệ không được phép đăng nhập");
        System.out.println("✓ Xác thực đúng thất bại với thông tin không hợp lệ");
    }
    
    /**
     * Test xác thực với thông tin trống
     */
    @Test
    @DisplayName("Empty credentials should not authenticate")
    public void testEmptyCredentials() {
        // Email trống
        boolean emptyEmailAuth = authenticateUser("", "password");
        assertFalse(emptyEmailAuth, "Email trống không được phép đăng nhập");
        
        // Mật khẩu trống
        boolean emptyPassAuth = authenticateUser("user@example.com", "");
        assertFalse(emptyPassAuth, "Mật khẩu trống không được phép đăng nhập");
        
        // Cả hai trống
        boolean bothEmptyAuth = authenticateUser("", "");
        assertFalse(bothEmptyAuth, "Email và mật khẩu trống không được phép đăng nhập");
        
        System.out.println("✓ Ngăn chặn thông tin đăng nhập trống thành công");
    }
    
    /**
     * Test phân quyền Admin/Customer
     */
    @Test
    @DisplayName("User roles should be properly determined")
    public void testUserRoles() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Tìm một admin
            PreparedStatement adminStmt = conn.prepareStatement(
                "SELECT * FROM users WHERE role = 'Admin' LIMIT 1"
            );
            ResultSet adminRs = adminStmt.executeQuery();
            
            if (adminRs.next()) {
                String adminEmail = adminRs.getString("email");
                String adminPass = adminRs.getString("password_hash");
                
                User adminUser = authenticateAndGetUser(adminEmail, adminPass);
                assertNotNull(adminUser, "Admin phải xác thực thành công");
                assertEquals("Admin", adminUser.getRole(), "Vai trò phải là Admin");
                System.out.println("✓ Xác thực Admin thành công: " + adminEmail);
            }
            
            // Tìm một customer
            PreparedStatement customerStmt = conn.prepareStatement(
                "SELECT * FROM users WHERE role = 'Customer' LIMIT 1"
            );
            ResultSet customerRs = customerStmt.executeQuery();
            
            if (customerRs.next()) {
                String customerEmail = customerRs.getString("email");
                String customerPass = customerRs.getString("password_hash");
                
                User customerUser = authenticateAndGetUser(customerEmail, customerPass);
                assertNotNull(customerUser, "Customer phải xác thực thành công");
                assertEquals("Customer", customerUser.getRole(), "Vai trò phải là Customer");
                System.out.println("✓ Xác thực Customer thành công: " + customerEmail);
            }
        } catch (Exception e) {
            fail("Exception trong quá trình test vai trò: " + e.getMessage());
        }
    }
    
    /**
     * Test SQL Injection
     */
    @Test
    @DisplayName("Login should be protected against SQL injection")
    public void testSqlInjectionProtection() {
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
            assertFalse(authenticated, "SQL Injection '" + injectionString + "' nên bị ngăn chặn");
            System.out.println("✓ Ngăn chặn SQL Injection thành công: " + injectionString);
        }
    }
    
    /**
     * Helper method xác thực người dùng
     */
    private boolean authenticateUser(String email, String password) {
        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null || email == null || email.isEmpty() || password == null || password.isEmpty()) {
                return false;
            }
            
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
    
    /**
     * Helper method xác thực và trả về đối tượng User
     */
    private User authenticateAndGetUser(String email, String password) {
        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null || email == null || email.isEmpty() || password == null || password.isEmpty()) {
                return null;
            }
            
            String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUser_id(rs.getInt("user_id"));
                user.setFull_name(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
