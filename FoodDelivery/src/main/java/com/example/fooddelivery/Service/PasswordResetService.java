package com.example.fooddelivery.Service;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.io.UnsupportedEncodingException;

import org.mindrot.jbcrypt.BCrypt;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import com.example.fooddelivery.Service.PasswordResetException;
import com.example.fooddelivery.Service.InvalidEmailFormatException;
import com.example.fooddelivery.Service.EmailNotFoundException; // Nếu bạn quyết định dùng
import com.example.fooddelivery.Service.TokenNotFoundException;
import com.example.fooddelivery.Service.TokenExpiredException;
import com.example.fooddelivery.Service.EmailSendingException;
import com.example.fooddelivery.Service.PasswordUpdateException;
import com.example.fooddelivery.Service.UserNotFoundException;

import com.example.fooddelivery.Database.DatabaseConnector;


public class PasswordResetOtpService {

    private final DatabaseConnector dbConnector;

    private static final int OTP_EXPIRATION_MINUTES = 10;
    private static final int OTP_LENGTH = 6;

    private static final String GMAIL_SMTP_HOST = "smtp.gmail.com";
    private static final String GMAIL_SMTP_PORT = "587"; // Port cho TLS
    private static final String GMAIL_USERNAME = "trangnor85@gmail.com"; // Email Gmail dùng để gửi
    private static final String GMAIL_ACCOUNT_PASSWORD = "iamnvt2005"; //
    private static final String SENDER_NAME = "AdminTeam";

    private static final SecureRandom random = new SecureRandom();


    public PasswordResetOtpService(DatabaseConnector dbConnector) {
        if (dbConnector == null) {
            throw new IllegalArgumentException("DatabaseConnector cannot be null");
        }
        this.dbConnector = dbConnector;
    }

    /**
     * Xử lý yêu cầu quên mật khẩu: Tạo OTP, lưu vào db, gửi email OTP.
     *
     * @param userEmail Email người dùng cung cấp.
     * @throws InvalidEmailFormatException Nếu email không hợp lệ.
     * @throws EmailSendingException Nếu lỗi gửi email.
     * @throws PasswordResetException Nếu lỗi lưu OTP hoặc lỗi db khác.
     * @throws SQLException Nếu có lỗi SQL không mong muốn.
     */
    public void requestPasswordResetOtp(String userEmail) throws PasswordResetException, SQLException {
        if (userEmail == null || userEmail.trim().isEmpty() || !userEmail.contains("@")) {
            throw new InvalidEmailFormatException("Định dạng email không hợp lệ: " + userEmail);
        }

        // Chỉ gửi mail nếu email tồn tại (kiểm tra trước)
        if (!emailExists(userEmail)) {
            System.out.println("DEBUG: Email không tồn tại, không gửi OTP: " + userEmail);

            return;
        }

        try {
            String otp = generateOtp(OTP_LENGTH);
            LocalDateTime expiresAt = LocalDateTime.now().plus(OTP_EXPIRATION_MINUTES, ChronoUnit.MINUTES);
            saveOrUpdateResetToken(userEmail, otp, expiresAt); // Lưu OTP vào cột 'token'
            sendOtpEmailViaGmailSmtp(userEmail, otp);           // Gửi email bằng mật khẩu chính
        } catch (SQLException e) {
            throw new PasswordResetException("Lỗi cơ sở dữ liệu khi xử lý yêu cầu OTP cho " + userEmail, e);
        } catch (EmailSendingException e) {
            throw e;
        } catch (Exception e) {
            throw new PasswordResetException("Lỗi không xác định khi yêu cầu OTP: " + e.getMessage(), e);
        }
    }

    /**
     * Xác thực mã OTP người dùng nhập vào.
     *
     * @param otp Mã OTP người dùng nhập.
     * @return Email nếu OTP hợp lệ và còn hạn.
     * @throws IllegalArgumentException Nếu định dạng OTP không hợp lệ.
     * @throws TokenNotFoundException Nếu OTP không đúng hoặc đã dùng.
     * @throws TokenExpiredException Nếu OTP đã hết hạn.
     * @throws SQLException Nếu lỗi truy vấn db.
     */
    public String validateOtp(String otp) throws TokenNotFoundException, TokenExpiredException, SQLException {
        if (otp == null || otp.trim().isEmpty() || !otp.matches("\\d{" + OTP_LENGTH + "}")) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ (phải gồm " + OTP_LENGTH + " chữ số).");
        }
        String sql = "SELECT email, expires_at FROM password_resets WHERE token = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String emailFromDb = rs.getString("email");
                    Timestamp expiresAtTimestamp = rs.getTimestamp("expires_at");
                    if (expiresAtTimestamp != null) {
                        LocalDateTime expiresAt = expiresAtTimestamp.toLocalDateTime();
                        if (expiresAt.isAfter(LocalDateTime.now())) {
                            return emailFromDb;
                        } else {
                            deleteOtpRecord(otp);
                            throw new TokenExpiredException("Mã OTP đã hết hạn.");
                        }
                    } else {
                        throw new SQLException("Dữ liệu OTP không hợp lệ (expires_at is null) for OTP: " + otp);
                    }
                } else {
                    throw new TokenNotFoundException("Mã OTP không đúng hoặc đã được sử dụng.");
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Đặt lại mật khẩu sau khi OTP đã được xác thực.
     *
     * @param validatedOtp Mã OTP đã được xác thực là hợp lệ.
     * @param newPassword Mật khẩu mới (plaintext).
     * @throws IllegalArgumentException Nếu mật khẩu mới không hợp lệ.
     * @throws UserNotFoundException Nếu không tìm thấy user để cập nhật.
     * @throws PasswordUpdateException Nếu có lỗi trong quá trình cập nhật hoặc xóa OTP.
     * @throws SQLException Nếu có lỗi SQL không mong muốn.
     * @throws TokenNotFoundException (Từ validateOtp nếu gọi lại)
     * @throws TokenExpiredException (Từ validateOtp nếu gọi lại)
     */
    public void resetPasswordWithOtp(String validatedOtp, String newPassword) throws PasswordResetException, SQLException {
        String email = validateOtp(validatedOtp);

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        String hashedPassword = hashPassword(newPassword);

        Connection conn = null;
        try {
            conn = dbConnector.getConnection();
            conn.setAutoCommit(false);
            updateUserPasswordInternal(conn, email, hashedPassword);
            deleteOtpRecordInternal(conn, validatedOtp);
            conn.commit();
        } catch (UserNotFoundException | SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { /* Log lỗi rollback */ }
            if (e instanceof UserNotFoundException) throw e;
            else throw new PasswordUpdateException("Lỗi SQL khi đặt lại mật khẩu bằng OTP cho email " + email, e);
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { /* Log lỗi rollback */ }
            throw new PasswordUpdateException("Lỗi không xác định khi đặt lại mật khẩu bằng OTP: " + e.getMessage(), e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* Log lỗi đóng conn */ }
        }
    }


    /**
     * Kiểm tra email tồn tại
     */
    private boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Tạo mã OTP ngẫu nhiên
     */
    private String generateOtp(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Lưu/Cập nhật OTP vào cột 'token' trong db
     */
    private void saveOrUpdateResetToken(String email, String otp, LocalDateTime expiresAt) throws SQLException {
        String deleteSql = "DELETE FROM password_resets WHERE email = ?";
        String insertSql = "INSERT INTO password_resets (email, token, expires_at) VALUES (?, ?, ?)";
        Connection conn = null;
        try {
            conn = dbConnector.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, email);
                deleteStmt.executeUpdate();
            }
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, email);
                insertStmt.setString(2, otp); // Lưu OTP
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
     * Gửi email chứa mã OTP qua Gmail SMTP.
     */
    private void sendOtpEmailViaGmailSmtp(String recipientEmail, String otp) throws EmailSendingException {
        final String username = "trangnor85@gmail.com";
        final String password = "iamnvt2005";

        Properties prop = new Properties();
        prop.put("mail.smtp.host", GMAIL_SMTP_HOST);
        prop.put("mail.smtp.port", GMAIL_SMTP_PORT);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); // TLS

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Trả về username và mật khẩu chính (đã lấy từ hằng số)
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username, SENDER_NAME, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("[FoodApp] Mã xác nhận đặt lại mật khẩu của bạn");

            String emailContent = String.format(
                    "Xin chào,\n\n" +
                            "Mã xác nhận để đặt lại mật khẩu của bạn là:\n\n" +
                            "   %s   \n\n" +
                            "Mã này sẽ hết hạn sau %d phút.\n\n" +
                            "Vui lòng nhập mã này vào ứng dụng để tiếp tục.\n" +
                            "Nếu bạn không yêu cầu thao tác này, vui lòng bỏ qua email.\n\n" +
                            "Trân trọng,\n%s",
                    otp, OTP_EXPIRATION_MINUTES, SENDER_NAME
            );
            message.setText(emailContent);

            Transport.send(message);
            System.out.println("INFO: Đã gửi OTP qua Gmail thành công tới: " + recipientEmail); // Tạm dùng println

        } catch (MessagingException e) {
            System.err.println("ERROR: Lỗi Jakarta Mail khi gửi OTP tới " + recipientEmail + ". Chi tiết: " + e.getMessage()); // Tạm dùng println
            throw new EmailSendingException("Lỗi khi gửi email OTP tới " + recipientEmail, e);
        } catch (UnsupportedEncodingException e) {
            System.err.println("ERROR: Lỗi encoding khi gửi OTP tới " + recipientEmail + ". Chi tiết: " + e.getMessage()); // Tạm dùng println
            throw new EmailSendingException("Lỗi encoding không hỗ trợ khi tạo địa chỉ gửi.", e);
        } catch (Exception e) {
            System.err.println("ERROR: Lỗi không xác định khi gửi OTP tới " + recipientEmail + ". Chi tiết: " + e.getMessage()); // Tạm dùng println
            throw new EmailSendingException("Lỗi không xác định khi gửi OTP tới " + recipientEmail, e);
        }
    }

    /**
     * Băm mật khẩu
     */
    private String hashPassword(String plainPassword) {
        // (Giữ nguyên logic)
        if (plainPassword == null) throw new IllegalArgumentException("Mật khẩu không được null.");
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Xóa OTP khỏi DB
     */
    private void deleteOtpRecordInternal(Connection conn, String otp) throws SQLException {
        String sql = "DELETE FROM password_resets WHERE token = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, otp);
            pstmt.executeUpdate();
        }
    }

    /**
     * Cập nhật mật khẩu user (Hàm nội bộ)
     */
    private void updateUserPasswordInternal(Connection conn, String email, String hashedPassword) throws SQLException, UserNotFoundException {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, email);
            if (pstmt.executeUpdate() == 0) {
                throw new UserNotFoundException("Không tìm thấy người dùng với email: " + email + " để cập nhật mật khẩu.");
            }
        }
    }
}