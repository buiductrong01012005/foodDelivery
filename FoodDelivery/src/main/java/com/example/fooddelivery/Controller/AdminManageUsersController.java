package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.UserDAO;
import com.example.fooddelivery.Model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminManageUsersController implements Initializable {

    @FXML private AnchorPane manageUserPaneRoot;

    // --- Khai báo @FXML cho controls trong ManageUsersView.fxml ---
    @FXML private TextField searchUserField;
    @FXML private TableView<User> userTableManage;
    @FXML private TableColumn<User, Integer> userIdColManage;
    @FXML private TableColumn<User, String> userNameColManage;
    @FXML private TableColumn<User, Integer> userAgeColManage;
    @FXML private TableColumn<User, String> userPhoneColManage;
    @FXML private TableColumn<User, String> userEmailColManage;
    @FXML private TableColumn<User, String> userAddressColManage;
    @FXML private TableColumn<User, String> userGenderColManage;
    @FXML private TableColumn<User, String> userRoleColManage;
    @FXML private Button addUserButton; // Nâng cấp Admin
    @FXML private Button detailsUserButton; // Xem chi tiết
    @FXML private Button editUserButton;    // Chỉnh sửa
    @FXML private Button deleteUserButton;  // Xóa

    private AdminContainerController adminContainerController; // Tham chiếu controller cha
    private ObservableList<User> allUsersList = FXCollections.observableArrayList(); // Danh sách đầy đủ
    private User currentAdminUser; // Admin đang đăng nhập
    private boolean initialDataLoaded = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminManageUsersController initialized.");
        configureUserTableManageColumns();
    }

    // --- Setters được gọi từ AdminContainerController ---
    public void setAdminContainerController(AdminContainerController controller) { this.adminContainerController = controller; }
    public void setCurrentAdminUser(User adminUser) { this.currentAdminUser = adminUser; }

    // --- Tải dữ liệu ---
    public void loadDataIfNeeded() {
        if (!initialDataLoaded) {
            System.out.println("AdminManageUsersController: Loading initial user data...");
            loadAndDisplayUserData();
            initialDataLoaded = true;
        } else {
            System.out.println("AdminManageUsersController: Data already loaded. Applying filter.");
            filterUserData(searchUserField.getText());
        }
    }

    public void refreshUsersTable() {
        System.out.println("AdminManageUsersController: Refreshing user data...");
        loadAndDisplayUserData(); // Tải lại toàn bộ
    }

    // --- Cấu hình bảng ---
    private void configureUserTableManageColumns() {
        System.out.println("AdminManageUsersController: Configuring table...");
        if (userTableManage == null) { System.err.println("AdminManageUsersController Error: TableView is null!"); return;}
        setupColumnFactory(userIdColManage, "user_id");
        setupColumnFactory(userNameColManage, "full_name");
        setupColumnFactory(userAgeColManage, "age"); // Sử dụng hàm getAge() từ Model
        setupColumnFactory(userPhoneColManage, "phone_number");
        setupColumnFactory(userEmailColManage, "email");
        setupColumnFactory(userAddressColManage, "address");
        setupColumnFactory(userGenderColManage, "gender");
        setupColumnFactory(userRoleColManage, "role");
        userTableManage.setPlaceholder(new Label("Chưa tải dữ liệu..."));
    }

    // Helper setup cột
    private <S, T> void setupColumnFactory(TableColumn<S, T> column, String propertyName) {
        if (column != null && propertyName != null && !propertyName.isEmpty()) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        } else {
            System.err.println("WARN (AdminManageUsers): Invalid column or propertyName for setupColumnFactory. Column: " + column + ", Property: " + propertyName);
        }
    }

    // --- Load và hiển thị dữ liệu ---
    void loadAndDisplayUserData() {
        if (userTableManage == null) return;
        userTableManage.setPlaceholder(new Label("Đang tải dữ liệu..."));

        new Thread(() -> {
            try {
                ObservableList<User> users = UserDAO.getAllUsers();
                Platform.runLater(() -> {
                    this.allUsersList.setAll(users != null ? users : FXCollections.observableArrayList());
                    System.out.println("AdminManageUsersController: User data fetched (" + allUsersList.size() + " users). Applying filter.");
                    filterUserData(searchUserField.getText());
                    userTableManage.setPlaceholder(new Label(allUsersList.isEmpty() ? "Không có người dùng nào." : ""));
                    userTableManage.getSelectionModel().clearSelection();
                });
            } catch (Exception e) {
                System.err.println("AdminManageUsersController Error loading users: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (userTableManage != null) userTableManage.setPlaceholder(new Label("Lỗi tải dữ liệu người dùng."));
                });
            }
        }).start();
    }

    // --- Lọc dữ liệu ---
    @FXML
    private void onSearchUserKeywordInput(ActionEvent event) {
        filterUserData(searchUserField.getText());
    }

    private void filterUserData(String keyword) {
        if (userTableManage == null) return;
        if (allUsersList.isEmpty() && (keyword == null || keyword.isEmpty())) {
            userTableManage.setItems(allUsersList);
            userTableManage.setPlaceholder(new Label("Không có người dùng nào."));
            return;
        }
        ObservableList<User> filteredList;
        String lowerCaseKeyword = (keyword == null) ? "" : keyword.toLowerCase().trim();
        if (lowerCaseKeyword.isEmpty()) {
            filteredList = allUsersList;
        } else {
            filteredList = allUsersList.stream()
                    .filter(user -> userMatchesKeyword(user, lowerCaseKeyword))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
        userTableManage.setItems(filteredList);
        userTableManage.setPlaceholder(new Label(filteredList.isEmpty() ? "Không tìm thấy kết quả phù hợp." : ""));
    }

    private boolean userMatchesKeyword(User user, String lowerCaseKeyword) {
        if (user == null) return false;
        return String.valueOf(user.getUser_id()).contains(lowerCaseKeyword) ||
                (user.getFull_name() != null && user.getFull_name().toLowerCase().contains(lowerCaseKeyword)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseKeyword)) ||
                (user.getPhone_number() != null && user.getPhone_number().contains(lowerCaseKeyword)) ||
                (user.getAddress() != null && user.getAddress().toLowerCase().contains(lowerCaseKeyword)) ||
                (user.getGender() != null && user.getGender().toLowerCase().contains(lowerCaseKeyword)) ||
                (user.getRole() != null && user.getRole().toLowerCase().contains(lowerCaseKeyword));
    }

    // --- Xử lý sự kiện nút ---
    @FXML
    private void onAddUserButtonClick(ActionEvent event) { // Nâng cấp Admin
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return;
        if ("Admin".equalsIgnoreCase(selectedUser.getRole())) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Người dùng này đã là Admin.");
            return;
        }
        confirmAndUpdateRole(selectedUser, "Admin");
    }

    @FXML
    private void onUserDetailsButtonClick(ActionEvent event) { // Xem chi tiết
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return;
        if (adminContainerController != null) {
            adminContainerController.requestUserView(selectedUser); // Gọi container
        } else { handleMissingContainerError(); }
    }

    @FXML
    private void onEditUserButtonClick(ActionEvent event) { // Chỉnh sửa
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return;
        if (adminContainerController != null) {
            adminContainerController.requestUserEdit(selectedUser); // Gọi container
        } else { handleMissingContainerError(); }
    }

    @FXML
    private void onDeleteUserButtonClick(ActionEvent event) { // Xóa
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return;
        if (currentAdminUser != null && selectedUser.getUser_id() == currentAdminUser.getUser_id()) {
            showAlert(Alert.AlertType.WARNING, "Không thể xóa", "Bạn không thể xóa chính tài khoản của mình.");
            return;
        }
        confirmAndDeleteUser(selectedUser);
    }

    // --- Các hàm helper (getSelectedUser, confirmAndUpdateRole, etc.) ---
    private User getSelectedUser() {
        if (userTableManage == null || userTableManage.getSelectionModel() == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Bảng người dùng không khả dụng.");
            return null;
        }
        User selected = userTableManage.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một người dùng trong bảng.");
        }
        return selected;
    }

    private void confirmAndUpdateRole(User user, String newRole) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn chuyển người dùng '" + user.getFull_name() + "' thành " + newRole + "?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Xác nhận");
        confirmation.setHeaderText("Nâng cấp vai trò");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            updateUserRoleAsync(user.getUser_id(), newRole, user.getFull_name());
        }
    }

    private void confirmAndDeleteUser(User user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa người dùng '" + user.getFull_name() + "' (ID: " + user.getUser_id() + ")?\nHành động này không thể hoàn tác.",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Xác nhận xóa");
        confirmation.setHeaderText("Xóa người dùng");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            deleteUserAsync(user.getUser_id(), user.getFull_name());
        }
    }

    private void updateUserRoleAsync(int userId, String newRole, String userName) {
        new Thread(() -> {
            boolean success = false;
            String message = "Lỗi không xác định khi cập nhật vai trò.";
            try {
                success = UserDAO.updateUserRole(userId, newRole);
                message = success ? "Đã nâng cấp '" + userName + "' thành " + newRole + "." : "Nâng cấp thất bại.";
            } catch (SQLException e) {
                message = "Lỗi SQL khi cập nhật vai trò: " + e.getMessage();
                success = false;
                e.printStackTrace();
            }
            final boolean finalSuccess = success;
            final String finalMessage = message;
            Platform.runLater(() -> {
                showAlert(finalSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, finalSuccess ? "Thành công" : "Thất bại", finalMessage);
                if (finalSuccess) {
                    refreshUsersTable(); // Load lại data nếu thành công
                }
            });
        }).start();
    }

    private void deleteUserAsync(int userId, String userName) {
        new Thread(() -> {
            boolean deleted = false;
            String message = "Lỗi không xác định khi xóa người dùng.";
            try {
                deleted = UserDAO.deleteUser(userId);
                message = deleted ? "Đã xóa người dùng '" + userName + "'."
                        : "Xóa thất bại (Người dùng không tồn tại hoặc có ràng buộc dữ liệu).";
            } catch (SQLException e) {
                message = "Lỗi SQL khi xóa người dùng: " + e.getMessage();
                if (e.getMessage().toLowerCase().contains("foreign key constraint")) {
                    message = "Không thể xóa '" + userName + "' do có dữ liệu liên quan.";
                }
                deleted = false;
                e.printStackTrace();
            }
            final boolean finalDeleted = deleted;
            final String finalMessage = message;
            Platform.runLater(() -> {
                showAlert(finalDeleted ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, finalDeleted ? "Thành công" : "Thất bại", finalMessage);
                if (finalDeleted) {
                    refreshUsersTable(); // Load lại data nếu thành công
                }
            });
        }).start();
    }

    private void handleMissingContainerError() {
        System.err.println("ERROR (AdminManageUsers): AdminContainerController is null. Cannot perform action.");
        showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể thực hiện hành động này.");
    }

    // Hàm showAlert tiện ích
    private void showAlert(Alert.AlertType alertType, String title, String content) {
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