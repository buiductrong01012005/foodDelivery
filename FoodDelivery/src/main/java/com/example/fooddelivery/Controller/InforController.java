package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.UserDAO;
import com.example.fooddelivery.Model.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class InforController implements Initializable {
    @FXML private AnchorPane userInfoPaneRoot;
    @FXML private Label infoPaneTitleLabel;
    @FXML private ImageView userImageViewInfo;

    @FXML private Label fullNameLabelInfo;
    @FXML private Label dobLabelInfo;
    @FXML private Label genderLabelInfo;
    @FXML private Label emailLabelInfo;
    @FXML private Label phoneLabelInfo;
    @FXML private Label roleLabelInfo;
    @FXML private Label addressLabelInfo;

    @FXML private TextField fullNameFieldInfo;
    @FXML private DatePicker dobPickerInfo;
    @FXML private ComboBox<String> genderComboBoxInfo;
    @FXML private TextField emailFieldInfo;
    @FXML private TextField phoneFieldInfo;
    @FXML private ComboBox<String> roleComboBoxInfo;
    @FXML private TextArea addressAreaInfo;

    @FXML private Button editAdminInfoButton;
    @FXML private Button closeInfoButton;
    @FXML private Button saveInfoButton;
    @FXML private Button cancelInfoButton;

    private User currentUser;
    private User loggedInAdmin;
    private String originalRole;
    private int currentAdminId;

    private AdminContainerController adminContainerController;


    private Image defaultProfileImage = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        defaultProfileImage = loadDefaultImage();
        userImageViewInfo.setImage(defaultProfileImage);

        genderComboBoxInfo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        roleComboBoxInfo.setItems(FXCollections.observableArrayList("Customer", "Admin"));
    }

    private Image loadDefaultImage() {
        String imagePath = "/images/profile_icon.png";
        try {
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream != null) {
                return new Image(imageStream);
            } else {
                System.err.println("Không tìm thấy ảnh mặc định tại resource: " + imagePath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh mặc định từ resource: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }

    public void setCurrentAdminId(int adminId) {
        this.currentAdminId = adminId;
    }

    public void setAdminContainerController(AdminContainerController controller) {
        this.adminContainerController = controller;
    }

    public void setCurrentAdminUser(User adminUser) {
        this.loggedInAdmin = adminUser;
    }


    public void displayUserInfo(User user, boolean isEditMode) {
        this.currentUser = user;

        if (currentUser == null) {
            handleNoUser();
            // Đảm bảo ảnh mặc định vẫn hiển thị khi không có user
            userImageViewInfo.setImage(defaultProfileImage);
            return;
        }
        this.originalRole = currentUser.getRole();

        userImageViewInfo.setImage(defaultProfileImage);
        populateDataFromCurrentUser();
        setUIMode(isEditMode);
    }

    private void handleNoUser() {
        clearAllFields();
        infoPaneTitleLabel.setText("Không có dữ liệu người dùng");
        setButtonsDisabled(true);
        userImageViewInfo.setImage(defaultProfileImage);
    }

    private void setButtonsDisabled(boolean disabled) {
        saveInfoButton.setDisable(disabled);
        editAdminInfoButton.setDisable(disabled);
        // Các nút khác có thể cần disable/enable tùy logic
        // closeInfoButton.setDisable(disabled);
        // cancelInfoButton.setDisable(disabled);
    }

    private void populateDataFromCurrentUser() {
        if (currentUser == null) return;

        fullNameLabelInfo.setText(getOrDefault(currentUser.getFull_name()));
        dobLabelInfo.setText(getOrDefault(currentUser.getDate_of_birth()));
        genderLabelInfo.setText(getOrDefault(currentUser.getGender()));
        emailLabelInfo.setText(getOrDefault(currentUser.getEmail()));
        phoneLabelInfo.setText(getOrDefault(currentUser.getPhone_number()));
        roleLabelInfo.setText(getOrDefault(currentUser.getRole()));
        addressLabelInfo.setText(getOrDefault(currentUser.getAddress()));

        fullNameFieldInfo.setText(currentUser.getFull_name());
        if (currentUser.getDate_of_birth() != null) {
            dobPickerInfo.setValue(currentUser.getDate_of_birth());
        } else {
            dobPickerInfo.setValue(null);
        }
        genderComboBoxInfo.setValue(currentUser.getGender());
        emailFieldInfo.setText(currentUser.getEmail());
        phoneFieldInfo.setText(currentUser.getPhone_number());
        roleComboBoxInfo.setValue(currentUser.getRole());
        addressAreaInfo.setText(currentUser.getAddress());

    }

    private String getOrDefault(Object value) {
        return value != null && !value.toString().trim().isEmpty() ? value.toString() : "[Chưa có]";
    }

    private void setUIMode(boolean isEditing) {
        infoPaneTitleLabel.setText(isEditing ? "Chỉnh Sửa Thông Tin Người Dùng" : "Thông Tin Chi Tiết Người Dùng");

        setElementVisibility(fullNameLabelInfo, !isEditing);
        setElementVisibility(fullNameFieldInfo, isEditing);
        setElementVisibility(dobLabelInfo, !isEditing);
        setElementVisibility(dobPickerInfo, isEditing);
        setElementVisibility(genderLabelInfo, !isEditing);
        setElementVisibility(genderComboBoxInfo, isEditing);
        setElementVisibility(emailLabelInfo, !isEditing);
        setElementVisibility(emailFieldInfo, isEditing);
        setElementVisibility(phoneLabelInfo, !isEditing);
        setElementVisibility(phoneFieldInfo, isEditing);
        setElementVisibility(roleLabelInfo, !isEditing);
        setElementVisibility(roleComboBoxInfo, isEditing);
        setElementVisibility(addressLabelInfo, !isEditing);
        setElementVisibility(addressAreaInfo, isEditing);

        setElementVisibility(saveInfoButton, isEditing);
        setElementVisibility(cancelInfoButton, isEditing);
        setElementVisibility(closeInfoButton, !isEditing);


        boolean isAdminViewingSelf = loggedInAdmin != null && currentUser != null && loggedInAdmin.getUser_id() == currentUser.getUser_id();
        setElementVisibility(editAdminInfoButton, !isEditing && isAdminViewingSelf);

        boolean canEditRole = loggedInAdmin != null && currentUser != null; //
        roleComboBoxInfo.setDisable(!isEditing || !canEditRole);


        if (currentUser != null) {
            setButtonsDisabled(false);
            saveInfoButton.setDisable(!isEditing);
            cancelInfoButton.setDisable(!isEditing);
            closeInfoButton.setDisable(isEditing);
            editAdminInfoButton.setDisable(isEditing || !isAdminViewingSelf);

        } else {
            setButtonsDisabled(true);
        }
    }

    private void setElementVisibility(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }


    @FXML
    void onSaveInfoButtonClick(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có dữ liệu người dùng để lưu.");
            return;
        }

        // --- VALIDATION (Thêm/Cải thiện) ---
        if (fullNameFieldInfo.getText() == null || fullNameFieldInfo.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Họ và tên không được để trống.");
            fullNameFieldInfo.requestFocus(); return;
        }
        if (dobPickerInfo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Ngày sinh không được để trống.");
            dobPickerInfo.requestFocus(); return;
        }
        if (dobPickerInfo.getValue().isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Ngày sinh không hợp lệ", "Ngày sinh không thể ở tương lai.");
            dobPickerInfo.requestFocus(); return;
        }
        if (genderComboBoxInfo.getValue() == null || genderComboBoxInfo.getValue().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Giới tính không được để trống.");
            genderComboBoxInfo.requestFocus(); return;
        }

        String email = emailFieldInfo.getText().trim();
        if (email.isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert(Alert.AlertType.WARNING, "Email không hợp lệ", "Vui lòng nhập địa chỉ email hợp lệ.");
            emailFieldInfo.requestFocus(); return;
        }

        String phone = phoneFieldInfo.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d+")) { // Chỉ cho phép số
            showAlert(Alert.AlertType.WARNING, "Số điện thoại không hợp lệ", "Số điện thoại chỉ được chứa chữ số.");
            phoneFieldInfo.requestFocus(); return;
        }


        String selectedRole = roleComboBoxInfo.getValue();
        if (selectedRole == null || selectedRole.trim().isEmpty()){
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vai trò không được để trống.");
            roleComboBoxInfo.requestFocus(); return;
        }

        if (!"Customer".equals(selectedRole) && !"Admin".equals(selectedRole)) {
            showAlert(Alert.AlertType.WARNING, "Vai trò không hợp lệ", "Vui lòng chọn 'Customer' hoặc 'Admin'.");
            roleComboBoxInfo.requestFocus(); return;
        }
        // --- END VALIDATION ---


        // --- UPDATE USER OBJECT ---
        currentUser.setFull_name(fullNameFieldInfo.getText().trim());
        currentUser.setDate_of_birth(dobPickerInfo.getValue());
        currentUser.setGender(genderComboBoxInfo.getValue());
        currentUser.setEmail(email);
        currentUser.setPhone_number(phone);
        currentUser.setAddress(addressAreaInfo.getText().trim());
        String newRole = roleComboBoxInfo.getValue();
        // --- END UPDATE USER OBJECT ---


        try {

            boolean generalInfoUpdated = UserDAO.updateUserGeneralInfo(currentUser);

            boolean roleUpdated = false;
            boolean roleUpdateAttempted = false;
            // Chỉ cập nhật role nếu nó thực sự thay đổi VÀ người dùng có quyền (kiểm tra lại quyền nếu cần)
            if (!originalRole.equals(newRole)) {

                roleUpdateAttempted = true;
                roleUpdated = UserDAO.updateUserRole(currentUser.getUser_id(), newRole);
                if (roleUpdated) {
                    currentUser.setRole(newRole);
                    originalRole = newRole;
                } else {
                    showAlert(Alert.AlertType.WARNING, "Cập nhật Vai trò Thất bại",
                            "Không thể cập nhật vai trò thành '" + newRole + "'. Vui lòng thử lại hoặc liên hệ quản trị viên.");
                    roleComboBoxInfo.setValue(originalRole);
                }
            }

            // Thông báo kết quả
            if (generalInfoUpdated || (roleUpdateAttempted && roleUpdated)) {
                String message = "Thông tin người dùng đã được cập nhật.";
                if (roleUpdateAttempted && !roleUpdated) {
                    message += "\nTuy nhiên, cập nhật vai trò không thành công.";
                }
                showAlert(Alert.AlertType.INFORMATION, "Thành công", message);
                if (adminContainerController != null) {
                    adminContainerController.finishedUserInfoAction();
                }

            } else if (roleUpdateAttempted && !roleUpdated && !generalInfoUpdated) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật", "Cập nhật vai trò thất bại và không có thông tin nào khác được thay đổi.");
            }
            else if (!generalInfoUpdated && !roleUpdateAttempted) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có thay đổi nào được lưu.");
                if (adminContainerController != null) {
                    adminContainerController.finishedUserInfoAction();
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Cơ Sở Dữ Liệu", "Lỗi khi cập nhật thông tin: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void onCancelInfoButtonClick(ActionEvent event) {

        if (adminContainerController != null) {
            adminContainerController.finishedUserInfoAction();
        }
    }

    @FXML
    void handleCloseInfoAction(ActionEvent event) {
        if (adminContainerController != null) {
            adminContainerController.finishedUserInfoAction();
        }
    }

    @FXML
    void onEditAdminInfoButtonClick(ActionEvent event) {
        if (currentUser != null && loggedInAdmin != null && currentUser.getUser_id() == loggedInAdmin.getUser_id()) {
            setUIMode(true);
        }
    }

    private void clearAllFields() {
        String na = "[N/A]";
        fullNameLabelInfo.setText(na);
        dobLabelInfo.setText(na);
        genderLabelInfo.setText(na);
        emailLabelInfo.setText(na);
        phoneLabelInfo.setText(na);
        roleLabelInfo.setText(na);
        addressLabelInfo.setText(na);

        userImageViewInfo.setImage(defaultProfileImage);

        fullNameFieldInfo.clear();
        dobPickerInfo.setValue(null);
        genderComboBoxInfo.getSelectionModel().clearSelection();

        emailFieldInfo.clear();
        phoneFieldInfo.clear();
        roleComboBoxInfo.getSelectionModel().clearSelection();
        addressAreaInfo.clear();

        emailFieldInfo.setEditable(true);
        roleComboBoxInfo.setDisable(false);

    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}