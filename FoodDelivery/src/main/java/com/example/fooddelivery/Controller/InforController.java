package com.example.fooddelivery.Controller;
import com.example.fooddelivery.Dao.UserDAO;
import com.example.fooddelivery.Model.User;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
public class InforController implements Initializable {
    @FXML private AnchorPane userInfoPaneRoot;
    @FXML private Label infoPaneTitleLabel;
    @FXML private ImageView userImageViewInfo; // FXML element, image display logic removed


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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        genderComboBoxInfo.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        roleComboBoxInfo.setItems(FXCollections.observableArrayList("Customer", "Admin"));
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
            return;
        }
        this.originalRole = currentUser.getRole();
        populateDataFromCurrentUser();
        setUIMode(isEditMode);
    }

    private void handleNoUser() {
        clearAllFields();
        infoPaneTitleLabel.setText("Không có dữ liệu người dùng");
        setButtonsDisabled(true);
    }

    private void setButtonsDisabled(boolean disabled) {
        saveInfoButton.setDisable(disabled);
        editAdminInfoButton.setDisable(disabled);
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
        dobPickerInfo.setValue(currentUser.getDate_of_birth());
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

        if (currentUser != null) {
            setButtonsDisabled(false);
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
        String selectedRole = roleComboBoxInfo.getValue();
        if (selectedRole == null || selectedRole.trim().isEmpty()){
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vai trò không được để trống.");
            roleComboBoxInfo.requestFocus(); return;
        }
        if (!"User".equals(selectedRole) && !"Admin".equals(selectedRole) && !"Customer".equals(selectedRole) /* If Customer is also valid for DAO */) {
            showAlert(Alert.AlertType.WARNING, "Vai trò không hợp lệ", "Vui lòng chọn 'User' hoặc 'Admin'.");
            roleComboBoxInfo.requestFocus(); return;
        }


        currentUser.setFull_name(fullNameFieldInfo.getText().trim());
        currentUser.setDate_of_birth(dobPickerInfo.getValue());
        currentUser.setGender(genderComboBoxInfo.getValue());
        currentUser.setPhone_number(phoneFieldInfo.getText().trim());
        currentUser.setAddress(addressAreaInfo.getText().trim());
        String newRole = roleComboBoxInfo.getValue();

        try {
            boolean generalInfoUpdated = UserDAO.updateUserGeneralInfo(currentUser);

            boolean roleUpdated = false;
            boolean roleUpdateAttempted = false;
            if (!originalRole.equals(newRole)) {
                roleUpdateAttempted = true;
                String roleForDAO = newRole;
                if ("User".equals(newRole) && !"Customer".equals(originalRole) && !"Admin".equals(originalRole) ) {
                    if(!newRole.equals("Admin")) roleForDAO = "Customer"; else roleForDAO = "Admin";
                }


                roleUpdated = UserDAO.updateUserRole(currentUser.getUser_id(), roleForDAO);
                if (roleUpdated) {
                    currentUser.setRole(newRole); // Update role in current object if DB succeeded
                } else {
                    showAlert(Alert.AlertType.WARNING, "Cập nhật Vai trò Thất bại",
                            "Không thể cập nhật vai trò. Vai trò '" + roleForDAO + "' có thể không hợp lệ hoặc có lỗi.");
                    roleComboBoxInfo.setValue(originalRole); // Revert UI
                }
            }

            if (generalInfoUpdated || (roleUpdateAttempted && roleUpdated) || (!roleUpdateAttempted && generalInfoUpdated) ) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thông tin người dùng đã được cập nhật.");
                if (adminContainerController != null) {
                    adminContainerController.finishedUserInfoAction();
                }
            } else if (roleUpdateAttempted && !roleUpdated && !generalInfoUpdated) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Cập Nhật", "Cập nhật vai trò thất bại và không có thông tin khác được thay đổi.");
            }
            else if (!generalInfoUpdated && !roleUpdateAttempted) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Không có thay đổi nào được thực hiện.");
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

        fullNameFieldInfo.clear();
        dobPickerInfo.setValue(null);
        genderComboBoxInfo.getSelectionModel().clearSelection();
        emailFieldInfo.clear();
        phoneFieldInfo.clear();
        roleComboBoxInfo.getSelectionModel().clearSelection();
        addressAreaInfo.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}