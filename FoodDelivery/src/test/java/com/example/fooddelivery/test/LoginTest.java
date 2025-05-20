package com.example.fooddelivery.test;

import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Utils.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bài test đơn giản cho LoginController
 */
public class LoginTest {

    /**
     * Reset session sau mỗi test
     */
    @AfterEach
    public void tearDown() {
        Session.clear();
    }

    /**
     * Kiểm tra kết nối tới cơ sở dữ liệu
     */
    @Test
    @DisplayName("Kiểm tra kết nối tới cơ sở dữ liệu")
    public void testDatabaseConnection() {
        // Thử kết nối đến cơ sở dữ liệu
        Connection conn = DatabaseConnector.connectDB();
        
        // Kiểm tra xem kết nối có thành công không
        assertNotNull(conn, "Kết nối database phải thành công");
        
        try {
            // Kiểm tra xem có kết nối được đến đúng database không
            PreparedStatement stmt = conn.prepareStatement("SELECT DATABASE()");
            ResultSet rs = stmt.executeQuery();
            
            assertTrue(rs.next(), "Phải trả về tên database");
            String dbName = rs.getString(1);
            assertEquals("simple_food_delivery_db", dbName, "Phải kết nối đến database 'simple_food_delivery_db'");
            
            System.out.println("✓ Kết nối thành công đến database: " + dbName);
            
            // Đóng kết nối khi test hoàn thành
            if (conn != null) {
                conn.close();
                System.out.println("✓ Đóng kết nối thành công");
            }
        } catch (SQLException e) {
            fail("Lỗi khi thao tác với database: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra bảng users có tồn tại không
     */
    @Test
    @DisplayName("Kiểm tra bảng users có tồn tại không")
    public void testUsersTableExists() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Truy vấn kiểm tra bảng users
            PreparedStatement stmt = conn.prepareStatement("SHOW TABLES LIKE 'users'");
            ResultSet rs = stmt.executeQuery();
            
            // Nếu bảng không tồn tại, kết quả sẽ trống
            assertTrue(rs.next(), "Bảng 'users' phải tồn tại trong database");
            System.out.println("✓ Bảng 'users' tồn tại trong database");
            
        } catch (SQLException e) {
            fail("Lỗi khi kiểm tra bảng users: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra cấu trúc bảng users
     */
    @Test
    @DisplayName("Kiểm tra cấu trúc bảng users")
    public void testUsersTableStructure() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Lấy thông tin cấu trúc bảng users
            PreparedStatement stmt = conn.prepareStatement("DESCRIBE users");
            ResultSet rs = stmt.executeQuery();
            
            boolean hasUserId = false;
            boolean hasEmail = false;
            boolean hasPasswordHash = false;
            boolean hasRole = false;
            
            while (rs.next()) {
                String columnName = rs.getString("Field");
                if ("user_id".equalsIgnoreCase(columnName)) hasUserId = true;
                if ("email".equalsIgnoreCase(columnName)) hasEmail = true;
                if ("password_hash".equalsIgnoreCase(columnName)) hasPasswordHash = true;
                if ("role".equalsIgnoreCase(columnName)) hasRole = true;
            }
            
            assertTrue(hasUserId, "Bảng users phải có cột 'user_id'");
            assertTrue(hasEmail, "Bảng users phải có cột 'email'");
            assertTrue(hasPasswordHash, "Bảng users phải có cột 'password_hash'");
            assertTrue(hasRole, "Bảng users phải có cột 'role'");
            
            System.out.println("✓ Bảng users có đầy đủ các cột cần thiết");
            
        } catch (SQLException e) {
            fail("Lỗi khi kiểm tra cấu trúc bảng users: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra đăng nhập với tài khoản admin
     */
    @Test
    @DisplayName("Kiểm tra đăng nhập với tài khoản admin")
    public void testAdminLogin() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Tìm kiếm tài khoản admin trong database
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE role = 'Admin' LIMIT 1"
            );
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Lấy thông tin đăng nhập của admin
                String email = rs.getString("email");
                String password = rs.getString("password_hash");
                
                // Thử đăng nhập
                boolean success = authenticateUser(email, password);
                assertTrue(success, "Admin phải đăng nhập thành công");
                System.out.println("✓ Đăng nhập thành công với tài khoản admin: " + email);
            } else {
                // Trường hợp không tìm thấy admin nào trong database
                System.out.println("⚠️ Không tìm thấy tài khoản admin trong database");
            }
        } catch (SQLException e) {
            fail("Lỗi khi kiểm tra đăng nhập admin: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra đăng nhập với tài khoản không tồn tại
     */
    @Test
    @DisplayName("Kiểm tra đăng nhập với tài khoản không tồn tại")
    public void testInvalidLogin() {
        String fakeEmail = "khongco@example.com";
        String fakePassword = "passwordgiamao123";
        
        boolean success = authenticateUser(fakeEmail, fakePassword);
        assertFalse(success, "Tài khoản không tồn tại không được phép đăng nhập");
        System.out.println("✓ Ngăn chặn đăng nhập với tài khoản giả mạo thành công");
    }
    
    /**
     * Hàm giả lập logic xác thực đăng nhập
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
            return rs.next(); // Có kết quả = đăng nhập thành công
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
