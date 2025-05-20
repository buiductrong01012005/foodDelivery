package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.User;
import com.example.fooddelivery.Utils.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cơ bản cho LoginController
 * Chỉ kiểm tra các chức năng không liên quan đến JavaFX
 */
public class LoginControllerSimpleTest {

    /**
     * Kiểm tra kết nối database có hoạt động không
     */
    @Test
    public void testDatabaseConnection() {
        // Thử kết nối đến cơ sở dữ liệu
        Connection conn = DatabaseConnector.connectDB();
        
        // Kết nối phải thành công và trả về đối tượng Connection
        assertNotNull(conn, "Kết nối database phải thành công");
        
        try {
            // Đóng kết nối khi test hoàn thành
            if (conn != null) {
                conn.close();
                System.out.println("✓ Kết nối database thành công và đã đóng.");
            }
        } catch (Exception e) {
            fail("Lỗi khi đóng kết nối: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra bảng users có tồn tại không
     */
    @Test
    public void testUsersTableExists() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Truy vấn thử bảng users
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
            ResultSet rs = stmt.executeQuery();
            
            // Nếu bảng không tồn tại, lệnh trên sẽ ném ra SQLException
            assertTrue(rs.next(), "Phải có kết quả trả về từ bảng users");
            int count = rs.getInt(1);
            System.out.println("✓ Bảng users tồn tại và có " + count + " bản ghi.");
            
        } catch (Exception e) {
            fail("Lỗi khi truy vấn bảng users: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra xác thực đăng nhập với thông tin hợp lệ
     */
    @Test
    public void testValidAuthentication() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Lấy thông tin của một user có sẵn trong database để test
            // Trong hệ thống thực tế, nên dùng dữ liệu test cố định
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users LIMIT 1");
            ResultSet userRs = checkStmt.executeQuery();
            
            if (userRs.next()) {
                String email = userRs.getString("email");
                String password = userRs.getString("password_hash");
                
                // Kiểm tra xác thực với thông tin tài khoản này
                boolean isAuthenticated = authenticateUser(email, password);
                assertTrue(isAuthenticated, "User với email " + email + " phải xác thực thành công");
                System.out.println("✓ Xác thực thành công với email: " + email);
            } else {
                fail("Không có bản ghi users nào trong database để test");
            }
            
        } catch (Exception e) {
            fail("Lỗi trong quá trình test xác thực: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra xác thực đăng nhập với thông tin không hợp lệ
     */
    @Test
    public void testInvalidAuthentication() {
        String invalidEmail = "khongtontai@example.com";
        String invalidPassword = "passwordkhonghople";
        
        boolean isAuthenticated = authenticateUser(invalidEmail, invalidPassword);
        assertFalse(isAuthenticated, "User không tồn tại không được xác thực");
        System.out.println("✓ Xác thực đúng thất bại với email không tồn tại: " + invalidEmail);
    }
    
    /**
     * Hàm helper thực hiện chức năng xác thực tương tự LoginController
     * nhưng không phụ thuộc vào JavaFX
     */
    private boolean authenticateUser(String email, String password) {
        try (Connection conn = DatabaseConnector.connectDB()) {
            if (conn == null) {
                return false;
            }

            String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            return rs.next(); // Nếu có kết quả trả về thì xác thực thành công
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Hàm xử lý sau mỗi test để đảm bảo dọn dẹp dữ liệu phiên
     */
    @AfterEach
    public void tearDown() {
        // Reset session sau mỗi test để tránh ảnh hưởng đến các test khác
        Session.clear();
    }
}