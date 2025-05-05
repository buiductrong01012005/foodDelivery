package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.UserDAO;
import com.example.fooddelivery.Model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox; // Thêm nếu cần
import javafx.stage.FileChooser;
import javafx.stage.Stage; // Thêm Stage

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class InforController implements Initializable { // Đổi tên lớp

    @FXML private AnchorPane userInfoPaneRoot; // ID của root Pane

    // --- Khai báo @FXML cho controls trong UserInfoView.fxml ---
    @FXML private Label infoPaneTitleLabel;
    @FXML private ImageView userImageViewInfo;
    @FXML private Button changeImageButton;
    @FXML private Label fullNameLabelInfo, dobLabelInfo, genderLabelInfo, emailLabelInfo, phoneLabelInfo, roleLabelInfo, addressLabelInfo;
    @FXML private TextField fullNameFieldInfo, emailFieldInfo, phoneFieldInfo;
    @FXML private DatePicker dobPickerInfo;
    @FXML private ComboBox<String> genderComboBoxInfo, roleComboBoxInfo;
    @FXML private TextArea addressAreaInfo;
    @FXML private Button editAdminInfoButton; // Nút để admin tự sửa TT
    @FXML private Button saveInfoButton;
    @FXML private Button cancelInfoButton;

    private AdminContainerController adminContainerController; // Tham chiếu controller cha
    private User currentUserDisplayed; // User đang được hiển thị/sửa
    private User currentAdminUser;     // Admin đang đăng nhập
    private boolean isEditMode = false; // Trạng thái hiện tại
    private String pendingImageFilePath = null;

    // --- Constants và Formatters ---
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final List<String> GENDER_OPTIONS = Arrays.asList("Nam", "Nữ", "Khác");
    private final List<String> ROLE_OPTIONS = Arrays.asList("Customer", "Shipper", "Admin");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("InforController initialized."); // Đổi tên lớp trong log
        // Khởi tạo ComboBoxes
        if (genderComboBoxInfo != null) genderComboBoxInfo.setItems(FXCollections.observableArrayList(GENDER_OPTIONS));
        if (roleComboBoxInfo != null) roleComboBoxInfo.setItems(FXCollections.observableArrayList(ROLE_OPTIONS));
        // Đặt pane về chế độ xem mặc định ban đầu
        configurePaneForMode(false);
    }

    // --- Setters ---
    public void setAdminContainerController(AdminContainerController controller) { this.adminContainerController = controller; }
    public void setCurrentAdminUser(User adminUser) {
        this.currentAdminUser = adminUser;
        updateEditAdminButtonVisibility(); // Cập nhật nút khi biết admin là ai
    }

    // --- Hiển thị / Chỉnh sửa ---
    public void displayUserInfo(User user, boolean editMode) {
        System.out.println("InforController: Displaying user " + (user != null ? user.getEmail() : "null") + " in " + (editMode ? "EDIT" : "VIEW") + " mode.");
        this.currentUserDisplayed = user;
        //this.isEditMode = editMode; // Sẽ được set trong configurePaneForMode
        this.pendingImageFilePath = null;
        configurePaneForMode(editMode);
        if (user == null) {
            clearPaneContents();
            if(infoPaneTitleLabel!=null) infoPaneTitleLabel.setText("Không có thông tin");
            return;
        }
        if(infoPaneTitleLabel!=null) infoPaneTitleLabel.setText(editMode ? "Chỉnh Sửa: " + user.getEmail() : "Thông Tin: " + user.getEmail());
        populateData(user);
        updateEditAdminButtonVisibility();
    }

    // --- Cấu hình và điền dữ liệu ---
    private void configurePaneForMode(boolean editMode) {
        this.isEditMode = editMode;

        // Logic ẩn/hiện controls và nút (giữ nguyên từ trước)
        setControlsVisibility(!editMode, fullNameLabelInfo, dobLabelInfo, genderLabelInfo, emailLabelInfo, phoneLabelInfo, roleLabelInfo, addressLabelInfo);
        setControlsVisibility(editMode, fullNameFieldInfo, dobPickerInfo, genderComboBoxInfo, emailFieldInfo, phoneFieldInfo, roleComboBoxInfo, addressAreaInfo, changeImageButton);
        if (emailFieldInfo != null) emailFieldInfo.setEditable(false);
        updateEditAdminButtonVisibility(); // Cập nhật nút sửa admin
        setControlsVisibility(editMode, saveInfoButton, cancelInfoButton);
    }

    private void updateEditAdminButtonVisibility() {
        boolean isAdminViewingSelf = !isEditMode && currentUserDisplayed != null && currentAdminUser != null && currentUserDisplayed.getUser_id() == currentAdminUser.getUser_id();
        setControlsVisibility(isAdminViewingSelf, editAdminInfoButton);
    }

    private void populateData(User user) {
        // Logic điền dữ liệu vào controls (giữ nguyên từ trước)
        if (user == null) { clearPaneContents(); return; } // An toàn hơn
        if (isEditMode) {
            if(fullNameFieldInfo!=null) fullNameFieldInfo.setText(user.getFull_name());
            if(dobPickerInfo!=null) dobPickerInfo.setValue(user.getDate_of_birth());
            if(genderComboBoxInfo!=null) genderComboBoxInfo.setValue(user.getGender());
            if(emailFieldInfo!=null) emailFieldInfo.setText(user.getEmail());
            if(phoneFieldInfo!=null) phoneFieldInfo.setText(user.getPhone_number());
            if(roleComboBoxInfo!=null) roleComboBoxInfo.setValue(user.getRole());
            if(addressAreaInfo!=null) addressAreaInfo.setText(user.getAddress());
        } else {
            if(fullNameLabelInfo!=null) fullNameLabelInfo.setText(getOrDefault(user.getFull_name()));
            if(dobLabelInfo!=null) dobLabelInfo.setText(user.getDate_of_birth() != null ? user.getDate_of_birth().format(dateFormatter) : "N/A");
            if(genderLabelInfo!=null) genderLabelInfo.setText(getOrDefault(user.getGender()));
            if(emailLabelInfo!=null) emailLabelInfo.setText(getOrDefault(user.getEmail()));
            if(phoneLabelInfo!=null) phoneLabelInfo.setText(getOrDefault(user.getPhone_number()));
            if(roleLabelInfo!=null) roleLabelInfo.setText(getOrDefault(user.getRole()));
            if(addressLabelInfo!=null) addressLabelInfo.setText(getOrDefault(user.getAddress(), "Chưa cập nhật"));
        }
        if (userImageViewInfo!=null) loadProfileImage(user.getProfile_picture_url());
    }

    private void clearPaneContents() {
        // Logic xóa nội dung (giữ nguyên từ trước)
        populateData(new User(0, "", "", "", null, "", "", "", "","")); // Điền giá trị rỗng/null
        if(userImageViewInfo!=null) userImageViewInfo.setImage(null);
        pendingImageFilePath = null;
    }

    private void setControlsVisibility(boolean isVisible, Control... controls) {
        // Logic ẩn/hiện (giữ nguyên)
        for (Control control : controls) {
            if (control != null) {
                control.setVisible(isVisible);
                control.setManaged(isVisible);
            }
        }
    }

    // --- Xử lý sự kiện nút ---
    @FXML
    void onEditAdminInfoButtonClick(ActionEvent event) {
        // Logic chuyển sang chế độ sửa cho admin (giữ nguyên)
        if (currentUserDisplayed != null && currentAdminUser != null && currentUserDisplayed.getUser_id() == currentAdminUser.getUser_id()) {
            System.out.println("InforController: Switching to edit mode for current admin.");
            configurePaneForMode(true);
            populateData(currentUserDisplayed);
        } else { System.err.println("InforController Error: EditAdminInfo button clicked but conditions not met."); }
    }

    @FXML
    void onSaveInfoButtonClick(ActionEvent event) {
        // Logic đọc, validate, tạo User, xử lý ảnh, gọi saveUserChangesAsync (giữ nguyên)
        if (currentUserDisplayed == null || !isEditMode) { /*...*/ return; }
        System.out.println("InforController: Saving changes for user: " + currentUserDisplayed.getEmail());
        String fullName = fullNameFieldInfo.getText().trim();
        LocalDate dob = dobPickerInfo.getValue();
        String gender = genderComboBoxInfo.getValue();
        String phone = phoneFieldInfo.getText().trim();
        String role = roleComboBoxInfo.getValue();
        String address = addressAreaInfo.getText().trim();
        if (!validateUserData(fullName, dob, gender, phone, role)) return;
        User updatedUser = new User(currentUserDisplayed.getUser_id(), fullName, currentUserDisplayed.getEmail(), currentUserDisplayed.getPassword_hash(), dob, phone, gender, currentUserDisplayed.getProfile_picture_url(), role, address);
        handleImageUpdate(updatedUser);
//        saveUserChangesAsync(updatedUser);
    }

    @FXML
    void onCancelInfoButtonClick(ActionEvent event) {
        // Logic gọi container để quay lại (giữ nguyên)
        System.out.println("InforController: Edit cancelled.");
        if (adminContainerController != null) {
            adminContainerController.finishedUserInfoAction();
        } else { handleMissingContainerError(); }
    }

    @FXML
    void onChangeImageButtonClick(ActionEvent event) {
        // Logic mở FileChooser (giữ nguyên)
        FileChooser fileChooser = new FileChooser(); /*...*/
        Stage stage = (Stage) (changeImageButton != null ? changeImageButton.getScene().getWindow() : null);
        if(stage == null) { /*...*/ return;}
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                String imagePath = selectedFile.toURI().toString();
                if (isValidURL(imagePath)) {
                    Image previewImage = new Image(imagePath);
                    if (!previewImage.isError()) {
                        if(userImageViewInfo!=null) userImageViewInfo.setImage(previewImage);
                        this.pendingImageFilePath = selectedFile.getAbsolutePath();
                        System.out.println("INFO: New profile image selected: " + pendingImageFilePath);
                    } else { showAlert(Alert.AlertType.ERROR, "Lỗi Ảnh", "File ảnh không hợp lệ."); this.pendingImageFilePath = null; }
                } else { showAlert(Alert.AlertType.ERROR, "Lỗi Ảnh", "Đường dẫn file ảnh không hợp lệ."); this.pendingImageFilePath = null; }
            } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Lỗi Ảnh", "Không thể tải ảnh xem trước: " + e.getMessage()); this.pendingImageFilePath = null; e.printStackTrace(); }
        } else { System.out.println("INFO: No new image selected."); }
    }

    // --- Các hàm helper (validate, handleImage, saveAsync, loadProfileImage, etc.) ---
    // Chuyển từ AdminController cũ vào đây
    private boolean validateUserData(String fullName, LocalDate dob, String gender, String phone, String role) {
        if (fullName.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Họ tên không được để trống."); return false; }
        if (dob == null) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày sinh không được để trống."); return false; }
        if (dob.isAfter(LocalDate.now())) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Ngày sinh không hợp lệ."); return false; }
        if (gender == null || gender.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn giới tính."); return false; }
        if (phone.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Số điện thoại không được để trống."); return false; }
        // Thêm validate định dạng SĐT nếu cần: if (!phone.matches("\\d{10,11}")) { ... }
        if (role == null || role.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn vai trò."); return false; }
        return true;
    }

    private void handleImageUpdate(User userToUpdate) {
        if (pendingImageFilePath != null && !pendingImageFilePath.isEmpty()) {
            System.out.println("INFO: Processing selected image: " + pendingImageFilePath);
            String newImageUrl = uploadImageAndGetUrl(pendingImageFilePath); // Hàm upload thực tế
            if (newImageUrl != null) {
                userToUpdate.setProfile_picture_url(newImageUrl);
                System.out.println("INFO: Profile picture URL updated to: " + newImageUrl);
            } else {
                showAlert(Alert.AlertType.WARNING, "Lỗi Upload Ảnh", "Không thể tải ảnh lên, ảnh đại diện chưa được cập nhật.");
            }
            pendingImageFilePath = null; // Reset
        }
    }

    // --- Placeholder cho hàm upload ảnh (CẦN CÀI ĐẶT THỰC TẾ) ---
    private String uploadImageAndGetUrl(String localFilePath) {
        System.out.println("WARN (InforController): Image upload function not implemented. Simulating upload for: " + localFilePath);
        try { return new File(localFilePath).toURI().toString(); } catch (Exception e) { return null; } // Chỉ để test
        // return null; // Trả về null nếu upload thất bại
    }

//    private void saveUserChangesAsync(User userToSave) {
//        ProgressIndicator savingIndicator = new ProgressIndicator(-1);
//        // Tạm thời thêm indicator vào HBox nút (cần làm đẹp hơn)
//        if (saveInfoButton!= null && saveInfoButton.getParent() instanceof HBox) ((HBox)saveInfoButton.getParent()).getChildren().add(savingIndicator);
//        setInfoPaneButtonsDisabled(true);
//
//        new Thread(() -> {
//            boolean success = false;
//            String message = "Lỗi không xác định khi cập nhật.";
//            try {
//                success = UserDAO.updateUser(userToSave);
//                message = success ? "Cập nhật thông tin thành công." : "Cập nhật thất bại.";
//                // Cập nhật lại currentAdminUser trong AdminContainer nếu admin tự sửa
//                if (success && currentAdminUser != null && userToSave.getUser_id() == currentAdminUser.getUser_id()) {
//                    Platform.runLater(() -> { if(adminContainerController!=null) adminContainerController.setCurrentAdminUser(userToSave); });
//                }
//            } catch (SQLException e) { message = "Lỗi SQL: " + e.getMessage(); success = false; e.printStackTrace(); }
//            catch (Exception e) { message = "Lỗi: " + e.getMessage(); success = false; e.printStackTrace(); }
//
//            final boolean finalSuccess = success; final String finalMessage = message;
//
//            Platform.runLater(() -> {
//                if (saveInfoButton!= null && saveInfoButton.getParent() instanceof HBox) ((HBox)saveInfoButton.getParent()).getChildren().remove(savingIndicator);
//                setInfoPaneButtonsDisabled(false);
//                showAlert(finalSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, finalSuccess ? "Thành công" : "Thất bại", finalMessage);
//                if(finalSuccess) {
//                    // Không cần reset isEditMode hay currentUserDisplayed ở đây nữa
//                    if(adminContainerController!=null) adminContainerController.finishedUserInfoAction(); // Thông báo cho cha
//                }
//            });
//        }).start();
//    }

    /** Helper disable/enable nút khi đang lưu */
    private void setInfoPaneButtonsDisabled(boolean disabled) {
        if(saveInfoButton != null) saveInfoButton.setDisable(disabled);
        if(cancelInfoButton != null) cancelInfoButton.setDisable(disabled);
        if(changeImageButton != null) changeImageButton.setDisable(disabled);
        if(editAdminInfoButton != null) editAdminInfoButton.setDisable(disabled);
    }

    // --- Load ảnh ---
    private void loadProfileImage(String imageUrl) {
        // Logic load ảnh (giữ nguyên)
        if (userImageViewInfo == null) return;
        if (imageUrl != null && !imageUrl.trim().isEmpty() && isValidURL(imageUrl)) {
            try {
                Image profileImage = new Image(imageUrl, true); // true để tải nền
                profileImage.errorProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) { Platform.runLater(this::setDefaultProfileImage); }
                });
                userImageViewInfo.setImage(profileImage);
                if (profileImage.isBackgroundLoading() && !profileImage.isError()) { /* Optional: Show placeholder */ }
                else if(profileImage.isError()){ setDefaultProfileImage(); }
            } catch (Exception e) { setDefaultProfileImage(); }
        } else { setDefaultProfileImage(); }
    }

    private void setDefaultProfileImage() {
        // Logic đặt ảnh mặc định (giữ nguyên)
        if (userImageViewInfo == null) return;
        try {
            String defaultImagePath = "/com/example/fooddelivery/images/default_avatar.png"; // Đảm bảo đường dẫn đúng
            URL defaultImageURL = getClass().getResource(defaultImagePath);
            if (defaultImageURL != null) { userImageViewInfo.setImage(new Image(defaultImageURL.toExternalForm())); }
            else { System.err.println("WARN (InforController): Default profile image not found."); userImageViewInfo.setImage(null); }
        } catch (Exception e) { System.err.println("ERROR (InforController): Could not load default profile image."); userImageViewInfo.setImage(null); }
    }

    // --- Helpers khác ---
    private String getOrDefault(String value) { return getOrDefault(value, "N/A"); }
    private String getOrDefault(String value, String defaultValue) { return (value != null && !value.trim().isEmpty()) ? value : defaultValue; }

    private void handleMissingContainerError() {
        System.err.println("ERROR (InforController): AdminContainerController is null. Cannot perform action.");
        showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể thực hiện hành động này.");
    }

    private boolean isValidURL(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (MalformedURLException e) {
            // Kiểm tra xem có phải là đường dẫn file hợp lệ không (cho test local)
            try {
                File f = new File(new URL(urlString).toURI());
                // Chỉ kiểm tra xem có tạo được File không, không cần exists()
                return true;
            } catch (Exception ex) {
                // Thử tạo File trực tiếp nếu không phải URI
                try {
                    new File(urlString);
                    return true; // Chấp nhận cả đường dẫn file local tuyệt đối/tương đối (cho test)
                } catch (Exception finalEx) {
                    System.err.println("WARN (InforController): Invalid URL or local file path: " + urlString);
                    return false;
                }
            }
        }
    }

    // Hàm showAlert tiện ích
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        // ... (Giống các controller khác)
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showAlertInternal(alertType, title, content));
        } else {
            showAlertInternal(alertType, title, content);
        }
    }
    private void showAlertInternal(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}