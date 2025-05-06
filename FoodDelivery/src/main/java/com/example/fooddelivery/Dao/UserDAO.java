package com.example.fooddelivery.Dao;

import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime; // Thêm nếu dùng cho created/updated_at

public class UserDAO {

    // Câu lệnh SELECT cơ bản với JOIN địa chỉ (dùng chung)
    private static final String BASE_USER_SELECT_QUERY =
            "SELECT u.*, a.street_address, a.ward, a.district, a.city " +
                    "FROM users u " +
                    "LEFT JOIN addresses a ON u.user_id = a.user_id AND a.is_default = 1 ";

    /**
     * Lấy danh sách TẤT CẢ người dùng cùng với địa chỉ mặc định của họ.
     * @return ObservableList chứa các đối tượng User.
     * @throws SQLException Nếu có lỗi truy vấn CSDL.
     */
    public static ObservableList<User> getAllUsers() throws SQLException {
        ObservableList<User> userList = FXCollections.observableArrayList();
        // Sử dụng câu lệnh SELECT cơ bản
        String query = BASE_USER_SELECT_QUERY;

        System.out.println("DEBUG: Executing getAllUsers query.");

        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (conn == null) throw new SQLException("Không thể kết nối tới CSDL.");

            // Lặp qua kết quả và map từng hàng thành đối tượng User
            while (rs.next()) {
                try {
                    User user = mapResultSetToUser(rs); // <<< GỌI HÀM HELPER
                    if (user != null) { // Kiểm tra null từ hàm map (dù không nên xảy ra nếu rs.next() true)
                        userList.add(user);
                    }
                } catch (SQLException mapEx) {
                    // Lỗi khi map một hàng cụ thể, ghi log và bỏ qua hàng đó
                    System.err.println("ERROR: Lỗi khi xử lý hàng dữ liệu user: " + mapEx.getMessage());
                    mapEx.printStackTrace();
                }
            }
            System.out.println("DEBUG: Finished processing result set. Found " + userList.size() + " users.");
        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi thực thi getAllUsers: " + e.getMessage());
            throw e; // Ném lại lỗi
        }
        return userList;
    }

    /**
     * Lấy thông tin chi tiết của một người dùng dựa vào ID.
     * @param userId ID của người dùng cần tìm.
     * @return Đối tượng User nếu tìm thấy, null nếu không.
     * @throws SQLException Nếu có lỗi SQL nghiêm trọng xảy ra.
     */
    public static User getUserById(int userId) throws SQLException {
        // Thêm điều kiện WHERE vào câu lệnh cơ bản
        String query = BASE_USER_SELECT_QUERY + "WHERE u.user_id = ?";
        User user = null;

        System.out.println("DEBUG: Attempting to get user by ID: " + userId);

        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) throw new SQLException("DB connection failed.");
            pstmt.setInt(1, userId); // Đặt tham số ID

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Nếu tìm thấy user
                    user = mapResultSetToUser(rs); // <<< GỌI HÀM HELPER
                } else {
                    System.out.println("INFO: No user found with ID: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi lấy user bằng ID " + userId + ": " + e.getMessage());
            throw e;
        }
        return user;
    }

    /**
     * Lấy thông tin chi tiết của một người dùng dựa vào địa chỉ email.
     * @param email Email của người dùng cần tìm.
     * @return Đối tượng User nếu tìm thấy, null nếu không.
     * @throws SQLException Nếu có lỗi SQL nghiêm trọng xảy ra.
     */
    public static User getUserByEmail(String email) throws SQLException {
        // Thêm điều kiện WHERE vào câu lệnh cơ bản
        String query = BASE_USER_SELECT_QUERY + "WHERE u.email = ?";
        User user = null;

        System.out.println("DEBUG: Attempting to get user by email: " + email);

        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (conn == null) throw new SQLException("DB connection failed.");
            pstmt.setString(1, email); // Đặt tham số email

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Nếu tìm thấy user
                    user = mapResultSetToUser(rs); // <<< GỌI HÀM HELPER
                } else {
                    System.out.println("INFO: No user found with email: " + email);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi lấy user bằng email " + email + ": " + e.getMessage());
            throw e;
        }
        return user;
    }

    // --- Phương thức cập nhật và xóa (giữ nguyên logic) ---

    public static boolean deleteUser(int userId) throws SQLException {
        // ... (code deleteUser như cũ) ...
        String sql = "DELETE FROM users WHERE user_id = ?";
        System.out.println("INFO: Attempting to delete user with ID: " + userId);
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Cannot connect to DB to delete user.");
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("INFO: Successfully deleted user with ID: " + userId); return true;
            } else {
                System.out.println("WARN: No user found with ID: " + userId + " to delete."); return false;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: SQL Error deleting user ID " + userId + ": " + e.getMessage());
            if (e.getErrorCode() == 1451) { System.err.println("WARN: Cannot delete user due to foreign key constraints."); }
            throw e;
        }
    }

    public static boolean updateUserRole(int userId, String newRole) throws SQLException {
        // ... (code updateUserRole như cũ) ...
        if (!"Admin".equalsIgnoreCase(newRole) && !"Customer".equalsIgnoreCase(newRole)) {
            System.err.println("ERROR: Invalid new role: " + newRole); return false;
        }
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        System.out.println("INFO: Attempting to update role for user ID: " + userId + " to: " + newRole);
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Cannot connect to DB to update role.");
            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("INFO: Successfully updated role for user ID: " + userId); return true;
            } else {
                System.out.println("WARN: No user found with ID: " + userId + " to update role."); return false;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: SQL Error updating role for user ID " + userId + ": " + e.getMessage());
            throw e;
        }
    }

    public static boolean updateAdminProfile(User user) throws SQLException {
        // ... (code updateAdminProfile như cũ, chỉ update bảng users) ...
        String sql = "UPDATE users SET full_name = ?, date_of_birth = ?, phone_number = ?, gender = ?, updated_at = NOW() WHERE user_id = ? AND role = 'Admin'";
        System.out.println("INFO: Attempting to update profile for Admin ID: " + user.getUser_id());
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (conn == null) throw new SQLException("Cannot connect to DB to update profile.");
            pstmt.setString(1, user.getFull_name());
            pstmt.setDate(2, (user.getDate_of_birth() != null) ? Date.valueOf(user.getDate_of_birth()) : null);
            pstmt.setString(3, user.getPhone_number());
            pstmt.setString(4, user.getGender());
            pstmt.setInt(5, user.getUser_id());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("INFO: Successfully updated profile for Admin ID: " + user.getUser_id()); return true;
            } else {
                System.out.println("WARN: No Admin found with ID: " + user.getUser_id() + " to update profile, or no changes."); return false;
            }
        } catch (SQLException e) {
            System.err.println("ERROR: SQL Error updating profile for Admin ID " + user.getUser_id() + ": " + e.getMessage());
            throw e;
        }
    }

    public static boolean updateUserGeneralInfo(User user) throws SQLException {
        // Include profile_picture_url if it's a direct column in 'users' and you handle it.
        // The current InforController version removed image handling, so profile_picture_url might not be changed by UI.
        String sql = "UPDATE users SET full_name = ?, date_of_birth = ?, phone_number = ?, gender = ?, profile_picture_url = ?, updated_at = NOW() " +
                "WHERE user_id = ?";
        // If profile_picture_url is NOT being updated by this method, remove it from the SQL and parameter setting.
        // If you also want to update a simple text address field from user.getAddress() in the users table:
        // String sql = "UPDATE users SET full_name = ?, date_of_birth = ?, phone_number = ?, gender = ?, address_text_column = ?, updated_at = NOW() WHERE user_id = ?";

        System.out.println("INFO: DAO Attempting to update general info for User ID: " + user.getUser_id());
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) throw new SQLException("Cannot connect to DB to update user info.");

            pstmt.setString(1, user.getFull_name());
            pstmt.setDate(2, (user.getDate_of_birth() != null) ? java.sql.Date.valueOf(user.getDate_of_birth()) : null);
            pstmt.setString(3, user.getPhone_number());
            pstmt.setString(4, user.getGender());
            pstmt.setString(5, user.getProfile_picture_url()); // Assumes profile_picture_url is part of the update
            // If not, remove this and adjust SQL & index below.

            // If updating a simple text address in 'users' table:
            // pstmt.setString(next_index, user.getAddress());
            // pstmt.setInt(next_index + 1, user.getUser_id());
            // Else (current version):
            pstmt.setInt(6, user.getUser_id());


            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("INFO: DAO Successfully updated general info for User ID: " + user.getUser_id());
                return true;
            } else {
                // This could mean no user found, or no data actually changed (all values were same as before)
                System.out.println("WARN: DAO No user found with ID: " + user.getUser_id() + " to update general info, or no actual data changes were made.");
                return false; // Or true if "no change" is not an error. Conventionally, false if 0 rows affected.
            }
        } catch (SQLException e) {
            System.err.println("ERROR: DAO SQL Error updating general info for User ID " + user.getUser_id() + ": " + e.getMessage());
            throw e;
        }
    }


    // ============================================================
    //                     Hàm Private Helpers
    // ============================================================

    /**
     * Ánh xạ dữ liệu từ một hàng trong ResultSet thành đối tượng User.
     * Hàm này giả định ResultSet đang trỏ đến một hàng hợp lệ (sau khi gọi rs.next()).
     * Nó cũng xử lý việc định dạng địa chỉ.
     * @param rs ResultSet đang trỏ đến hàng dữ liệu user.
     * @return Đối tượng User đã được tạo.
     * @throws SQLException Nếu có lỗi khi đọc dữ liệu từ ResultSet.
     */
    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        // Lấy thông tin cơ bản của User
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
        // Thêm lấy created_at, updated_at nếu Model User có
        // Timestamp createdAtTs = rs.getTimestamp("created_at");
        // LocalDateTime createdAt = (createdAtTs != null) ? createdAtTs.toLocalDateTime() : null;
        // Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        // LocalDateTime updatedAt = (updatedAtTs != null) ? updatedAtTs.toLocalDateTime() : null;

        // Lấy thông tin địa chỉ từ JOIN (có thể là NULL)
        String street = rs.getString("street_address");
        String ward = rs.getString("ward");
        String district = rs.getString("district");
        String city = rs.getString("city");

        // Định dạng địa chỉ thành chuỗi
        String formattedAddress = formatAddress(street, ward, district, city);

        // Tạo đối tượng User với địa chỉ đã định dạng
        // Đảm bảo constructor của User khớp với các tham số này
        User user = new User(id, name, email, pass, dob, phone, gender, profile, role, formattedAddress);
        // user.setCreatedAt(createdAt); // Nếu có setter
        // user.setUpdatedAt(updatedAt); // Nếu có setter

        return user;
    }


    /**
     * Hàm helper để định dạng địa chỉ thành một chuỗi.
     */
    private static String formatAddress(String street, String ward, String district, String city) {
        StringBuilder addressBuilder = new StringBuilder();
        boolean firstPart = true;
        // Thêm các phần của địa chỉ nếu chúng không null hoặc rỗng
        if (street != null && !street.trim().isEmpty()) {
            addressBuilder.append(street.trim()); firstPart = false;
        }
        if (ward != null && !ward.trim().isEmpty()) {
            if (!firstPart) addressBuilder.append(", ");
            addressBuilder.append(ward.trim()); firstPart = false;
        }
        if (district != null && !district.trim().isEmpty()) {
            if (!firstPart) addressBuilder.append(", ");
            addressBuilder.append(district.trim()); firstPart = false;
        }
        if (city != null && !city.trim().isEmpty()) {
            if (!firstPart) addressBuilder.append(", ");
            addressBuilder.append(city.trim());
        }
        // Trả về chuỗi đã định dạng hoặc "N/A" nếu không có thông tin
        return addressBuilder.length() > 0 ? addressBuilder.toString() : "N/A";
    }

} // --- Kết thúc lớp UserDAO ---