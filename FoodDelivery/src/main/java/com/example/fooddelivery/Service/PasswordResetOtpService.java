package com.example.fooddelivery.Service; // Hoặc package của bạn

import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt; // Hash mật khẩu

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.fooddelivery.Database.DatabaseConnector;

public class PasswordResetOtpService {

    private final DatabaseConnector dbConnector;
    private static final int OTP_EXPIRATION_MINUTES = 10;
    public static final int OTP_LENGTH = 6;

    private static final String MAILJET_API_KEY = "7c1c3ec19a805337f08f9a2858837ba7";
    private static final String MAILJET_SECRET_KEY = "859467bf57bd288bf221b227b549519c";
    private static final String SENDER_EMAIL = "trangnor85@gmail.com";
    private static final String SENDER_NAME = "FoodApp Team"; //

    private static final SecureRandom random = new SecureRandom();

    public PasswordResetOtpService(DatabaseConnector dbConnector) {
        if (dbConnector == null) {
            throw new IllegalArgumentException("DatabaseConnector cannot be null");
        }
        this.dbConnector = dbConnector;
    }

    /**
     * Xử lý yêu cầu quên mật khẩu: Tạo OTP, lưu DB, gửi email OTP qua Mailjet.
     *
     * @param userEmail Email người dùng cung cấp.
     * @return true nếu yêu cầu được xử lý (mail đã được cố gắng gửi nếu email tồn tại),
     *         false nếu có lỗi nghiêm trọng (lỗi DB khi lưu OTP hoặc lỗi Mailjet không gửi được).
     * @throws SQLException Nếu có lỗi SQL không mong muốn xảy ra trong quá trình lưu OTP.
     */
    public boolean requestPasswordResetOtp(String userEmail) throws SQLException {
        if (userEmail == null || userEmail.trim().isEmpty() || !userEmail.contains("@")) {
            System.err.println("Lỗi: Định dạng email không hợp lệ: " + userEmail);
            return false; // Trả về false cho lỗi định dạng
        }

        boolean emailActuallyExists = false;
        try {
            emailActuallyExists = emailExists(userEmail);
        } catch (SQLException e){
            System.err.println("ERROR: Lỗi SQL khi kiểm tra email tồn tại cho " + userEmail);
            throw e; // Ném lại lỗi SQL nghiêm trọng
        }

        if (!emailActuallyExists) {
            System.out.println("INFO: Email không tồn tại, không gửi OTP: " + userEmail);
            return true; // Vẫn trả về true để ẩn thông tin
        }

        String otp = ""; // Khởi tạo để dùng trong khối catch
        try {
            otp = generateOtp(OTP_LENGTH);
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

            saveOrUpdateResetToken(userEmail, otp, expiresAt);

            boolean mailSent = sendOtpEmailViaMailjet(userEmail, otp); // Gọi hàm Mailjet
            if (!mailSent){
                System.err.println("WARN: Gửi email OTP qua Mailjet thất bại cho " + userEmail);
                return false;
            }

            return true;

        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi lưu OTP cho " + userEmail);
            throw e;
        } catch (Exception e) {
            System.err.println("ERROR: Lỗi không xác định khi yêu cầu OTP cho " + userEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // --- Hàm validateOtp ---

    public String validateOtp(String otp) throws SQLException {
        if (otp == null || otp.trim().isEmpty() || !otp.matches("\\d{" + OTP_LENGTH + "}")) {
            System.err.println("Lỗi: Định dạng OTP không hợp lệ.");
            return null;
        }
        String sql = "SELECT email, expires_at FROM password_resets WHERE token = ?";
        try (Connection conn = dbConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp expiresAtTimestamp = rs.getTimestamp("expires_at");
                    if (expiresAtTimestamp != null && expiresAtTimestamp.toLocalDateTime().isAfter(LocalDateTime.now())) {
                        return rs.getString("email"); // Hợp lệ
                    }
                    // Hết hạn hoặc null
                    System.out.println("INFO: OTP hết hạn hoặc lỗi dữ liệu: " + otp);
                    try { deleteOtpRecord(otp); } catch (SQLException eDel) { /* Log lỗi xóa */ }
                } else {
                    System.out.println("INFO: OTP không đúng hoặc đã sử dụng: " + otp);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi xác thực OTP: " + otp);
            throw e;
        }
        return null;
    }


    // --- Hàm resetPasswordWithOtp ---

    public boolean resetPasswordWithOtp(String otp, String newPassword) throws SQLException {
        String email = getEmailFromOtp(otp);
        if (email == null) {
            System.err.println("ERROR: Không tìm thấy email tương ứng với OTP hợp lệ khi reset: " + otp);
            return false;
        }
        if (newPassword == null || newPassword.length() < 6) {
            System.err.println("Lỗi: Mật khẩu mới phải có ít nhất 6 ký tự.");
            return false;
        }
        String hashedPassword = hashPassword(newPassword);
        Connection conn = null;
        boolean success = false;
        try {
            conn = dbConnector.connectDB();
            conn.setAutoCommit(false);
            boolean userUpdated = updateUserPasswordInternal(conn, email, hashedPassword);
            if (!userUpdated) {
                throw new SQLException("Không tìm thấy user với email " + email + " để cập nhật.");
            }
            deleteOtpRecordInternal(conn, otp);
            conn.commit();
            success = true;
            System.out.println("INFO: Đặt lại mật khẩu thành công cho: " + email);
        } catch (SQLException e) {
            System.err.println("ERROR: Lỗi SQL khi đặt lại mật khẩu bằng OTP cho email " + email);
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { /* Log */ }
            throw e;
        } catch (Exception e){
            System.err.println("ERROR: Lỗi không xác định khi đặt lại mật khẩu bằng OTP: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { /* Log */ }
            success = false;
        }
        finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* Log */ }
        }
        return success;
    }


    /** Kiểm tra email tồn tại */
    private boolean emailExists(String email) throws SQLException {
        // (Giữ nguyên)
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = dbConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Tạo mã OTP ngẫu nhiên */
    private String generateOtp(int length) {
        // (Giữ nguyên)
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /** Lưu/Cập nhật OTP vào cột 'token' */
    private void saveOrUpdateResetToken(String email, String otp, LocalDateTime expiresAt) throws SQLException {
        // (Giữ nguyên)
        String deleteSql = "DELETE FROM password_resets WHERE email = ?";
        String insertSql = "INSERT INTO password_resets (email, token, expires_at) VALUES (?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnector.connectDB();
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, email);
                deleteStmt.executeUpdate();
            }
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, email);
                insertStmt.setString(2, otp);
                insertStmt.setTimestamp(3, Timestamp.valueOf(expiresAt));
                if (insertStmt.executeUpdate() == 0) {
                    throw new SQLException("Lưu OTP reset không thành công.");
                }
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { /* Log */ }
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* Log */ }
        }
    }

    /**
     * Gửi email chứa mã OTP qua Mailjet API.
     * @return true nếu gửi thành công (theo Mailjet), false nếu thất bại.
     */
    private boolean sendOtpEmailViaMailjet(String recipientEmail, String otp) {
        System.out.println("DEBUG: Chuẩn bị gửi OTP qua Mailjet tới: " + recipientEmail);

        ClientOptions options = ClientOptions.builder()
                .apiKey(MAILJET_API_KEY)
                .apiSecretKey(MAILJET_SECRET_KEY)
                .build();
        MailjetClient client = new MailjetClient(options);

        String emailSubject = "[FoodApp] Mã xác nhận đặt lại mật khẩu của bạn";
        String emailTextPart = String.format( /* ... Nội dung text với OTP ... */ otp, OTP_EXPIRATION_MINUTES, SENDER_NAME);
        String emailHtmlPart = String.format( /* ... Nội dung HTML với OTP ... */ otp, OTP_EXPIRATION_MINUTES, SENDER_NAME);

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", SENDER_EMAIL) // Email đã xác thực
                                        .put("Name", SENDER_NAME))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", recipientEmail)
                                        ))
                                .put(Emailv31.Message.SUBJECT, emailSubject)
                                .put(Emailv31.Message.TEXTPART, emailTextPart)
                                .put(Emailv31.Message.HTMLPART, emailHtmlPart) // Nên có cả HTML cho đẹp
                        ));
        try {
            MailjetResponse response = client.post(request);
            if (response.getStatus() == 200) {
                System.out.println("INFO: Gửi OTP qua Mailjet thành công tới: " + recipientEmail);
                return true;
            } else {
                // Log lỗi chi tiết từ Mailjet
                System.err.println("ERROR: Gửi email qua Mailjet thất bại. Status: " + response.getStatus() + " | Response: " + response.getData());
                return false;
            }
        } catch (MailjetException e) {
            System.err.println("ERROR: Lỗi Mailjet Exception khi gửi OTP: " + e.getMessage());
            e.printStackTrace(); // Log stack trace
            return false;
        } catch (Exception e) { // Bắt lỗi khác
            System.err.println("ERROR: Lỗi không xác định khi gửi OTP qua Mailjet: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /** Băm mật khẩu */
    private String hashPassword(String plainPassword) {
        // (Giữ nguyên)
        if (plainPassword == null) throw new IllegalArgumentException("Mật khẩu không được null.");
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /** Xóa OTP khỏi DB (Hàm nội bộ) */
    private void deleteOtpRecordInternal(Connection conn, String otp) throws SQLException {
        // (Giữ nguyên)
        String sql = "DELETE FROM password_resets WHERE token = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            pstmt.executeUpdate();
        }
    }

    /** Cập nhật mật khẩu user (Hàm nội bộ) */
    private boolean updateUserPasswordInternal(Connection conn, String email, String hashedPassword) throws SQLException {
        // (Giữ nguyên)
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
        }
    }

    /** Lấy email từ OTP hợp lệ */
    private String getEmailFromOtp(String otp) throws SQLException {
        // (Giữ nguyên)
        String sql = "SELECT email FROM password_resets WHERE token = ? AND expires_at > NOW()";
        try (Connection conn = dbConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("email");
            }
        }
        return null;
    }

    /** Xóa OTP (Hàm public nếu cần) */
    public void deleteOtpRecord(String otp) throws SQLException {
        // (Giữ nguyên)
        String sql = "DELETE FROM password_resets WHERE token = ?";
        try (Connection conn = dbConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            pstmt.executeUpdate();
        }
    }
}
