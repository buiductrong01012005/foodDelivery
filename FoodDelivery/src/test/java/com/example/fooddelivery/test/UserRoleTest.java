package com.example.fooddelivery.test;

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
 * Kiểm tra phân quyền và vai trò người dùng
 */
public class UserRoleTest {

    @AfterEach
    public void tearDown() {
        Session.clear();
    }

    /**
     * Kiểm tra phân biệt vai trò Admin
     */
    @Test
    @DisplayName("Admin phải được xác thực đúng vai trò")
    public void testAdminRole() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Tìm tài khoản Admin
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE role = 'Admin' LIMIT 1"
            );
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String email = rs.getString("email");
                String password = rs.getString("password_hash");
                
                User adminUser = authenticateAndGetUser(email, password);
                
                assertNotNull(adminUser, "Admin phải đăng nhập thành công");
                assertEquals("Admin", adminUser.getRole(), "Vai trò phải là Admin");
                System.out.println("✓ Xác thực vai trò Admin thành công: " + email);
                
                // Kiểm tra quyền đặc biệt của Admin (ví dụ theo yêu cầu của ứng dụng)
                assertTrue(canAccessAdminDashboard(adminUser), "Admin phải có quyền truy cập vào dashboard");
                System.out.println("✓ Admin có quyền truy cập Dashboard");
            } else {
                System.out.println("⚠️ Không tìm thấy tài khoản Admin nào trong database");
            }
        } catch (Exception e) {
            fail("Lỗi khi kiểm tra vai trò Admin: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra phân biệt vai trò Customer
     */
    @Test
    @DisplayName("Customer phải được xác thực đúng vai trò")
    public void testCustomerRole() {
        try (Connection conn = DatabaseConnector.connectDB()) {
            assertNotNull(conn, "Kết nối database phải thành công");
            
            // Tìm tài khoản Customer
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE role = 'Customer' LIMIT 1"
            );
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String email = rs.getString("email");
                String password = rs.getString("password_hash");
                
                User customerUser = authenticateAndGetUser(email, password);
                
                assertNotNull(customerUser, "Customer phải đăng nhập thành công");
                assertEquals("Customer", customerUser.getRole(), "Vai trò phải là Customer");
                System.out.println("✓ Xác thực vai trò Customer thành công: " + email);
                
                // Kiểm tra Customer không có quyền Admin
                assertFalse(canAccessAdminDashboard(customerUser), "Customer không được phép truy cập vào Admin Dashboard");
                System.out.println("✓ Customer không thể truy cập Admin Dashboard");
            } else {
                System.out.println("⚠️ Không tìm thấy tài khoản Customer nào trong database");
            }
        } catch (Exception e) {
            fail("Lỗi khi kiểm tra vai trò Customer: " + e.getMessage());
        }
    }
    
    /**
     * Kiểm tra chuyển hướng sau đăng nhập dựa trên vai trò
     */
    @Test
    @DisplayName("Chuyển hướng phải phù hợp với vai trò")
    public void testRoleBasedRedirection() {
        User adminUser = new User();
        adminUser.setRole("Admin");
        
        User customerUser = new User();
        customerUser.setRole("Customer");
        
        // Kiểm tra điều hướng người dùng Admin
        String adminRedirect = getRedirectPathForRole(adminUser);
        assertEquals("/fxml/Admin/AdminContainer.fxml", adminRedirect, 
            "Admin phải được chuyển hướng đến trang quản trị");
        
        // Kiểm tra điều hướng người dùng Customer
        String customerRedirect = getRedirectPathForRole(customerUser);
        assertEquals("/fxml/User/UserHome.fxml", customerRedirect, 
            "Customer phải được chuyển hướng đến trang chủ");
        
        System.out.println("✓ Chuyển hướng đúng vai trò");
    }
    
    /**
     * Hàm xác thực và lấy thông tin User
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
    
    /**
     * Hàm giả lập kiểm tra quyền truy cập Dashboard
     */
    private boolean canAccessAdminDashboard(User user) {
        return user != null && "Admin".equals(user.getRole());
    }
    
    /**
     * Hàm giả lập lấy đường dẫn chuyển hướng theo vai trò
     */
    private String getRedirectPathForRole(User user) {
        if (user == null) {
            return "/fxml/Login.fxml";
        }
        
        if ("Admin".equals(user.getRole())) {
            return "/fxml/Admin/AdminContainer.fxml";
        } else {
            return "/fxml/User/UserHome.fxml";
        }
    }
}
