package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.UserDAO;
import com.example.fooddelivery.Main;
import com.example.fooddelivery.Model.User;
import com.example.fooddelivery.Database.DatabaseConnector;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional; // Cần cho Confirmation Alert
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminController implements Initializable {

    // --- Khai báo khớp fx:id từ FXML ---
    @FXML private StackPane contentStackPane;
    @FXML private AnchorPane dashboardPane;
    @FXML private AnchorPane manageUserPane;
    @FXML private AnchorPane manageFoodPane;

    @FXML private Button dashboardBtn;
    @FXML private Button foodBtn;
    @FXML private Button orderBtn;
    @FXML private Button userBtn;
    @FXML private Button inforBtn;
    @FXML private Button logOutBtn;

    // --- Manage User Components ---
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
    @FXML private Button addUserButton;      // Nút "Thêm mới" (Đổi role)
    @FXML private Button editUserButton;     // Nút "Chỉnh sửa"
    @FXML private Button deleteUserButton;   // Nút "Xoá"

    // (Khai báo các @FXML khác cho Dashboard/Food nếu cần)

    // --- Dữ liệu ---
    private ObservableList<User> allUsersList; // Danh sách đầy đủ user để lọc

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing AdminController...");
        // Không cần khởi tạo DB Connector nếu DAO dùng static connect

        // Cấu hình các cột cho bảng quản lý người dùng
        configureUserTableColumns();

        // Tải dữ liệu và hiển thị màn hình USER MANAGE mặc định
        showManageUsers(null);

        System.out.println("AdminController initialized. Default view: Manage Users.");
    }

    // --- Phương thức chuyển đổi Pane (Giữ nguyên) ---
    private void showPane(AnchorPane paneToShow) {
        if (contentStackPane == null) { return; }
        contentStackPane.getChildren().forEach(node -> {
            if (node instanceof AnchorPane) {
                node.setVisible(false);
                node.setManaged(false);
            }
        });
        if (paneToShow != null) {
            paneToShow.setVisible(true);
            paneToShow.setManaged(true);
            System.out.println("INFO: Showing pane: " + paneToShow.getId());
        } else {
            System.err.println("WARN: Pane to show is null!");
        }
    }

    // --- Các phương thức xử lý sự kiện từ SideBar (Giữ nguyên hoặc cập nhật nếu cần load data) ---
    @FXML
    private void showDashboard(ActionEvent event) {
        System.out.println("INFO: Switching to Dashboard view...");
        showPane(dashboardPane);
        // TODO: Load dashboard data if needed
    }

    @FXML
    private void showManageFood(ActionEvent event) {
        System.out.println("INFO: Switching to Manage Food view...");
        showPane(manageFoodPane);
        // TODO: Load food data if needed
        showAlert(Alert.AlertType.INFORMATION,"Thông báo","Chức năng Quản lý Món ăn đang được phát triển.");
    }

    @FXML
    private void showManageOrders(ActionEvent event) {
        System.out.println("WARN: Chức năng Quản lý Đơn hàng chưa được triển khai.");
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Quản lý Đơn hàng đang được phát triển.");
    }

    @FXML
    private void showManageUsers(ActionEvent event) {
        System.out.println("INFO: Switching to Manage Users view...");
        showPane(manageUserPane);
        loadAndDisplayUserData(); // Load/Reload dữ liệu khi chuyển đến tab này
    }

    @FXML
    private void showAdminInfo(ActionEvent event) {
        System.out.println("WARN: Chức năng Thông tin Admin chưa được triển khai.");
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Thông tin Admin đang được phát triển.");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("INFO: Admin logging out...");
        Main.showLoginView();
    }

    // --- Các phương thức cấu hình và load data cho màn hình Quản lý User ---

    private void configureUserTableColumns() {
        if (userTableManage == null) {
            System.err.println("ERROR: userTableManage is null during column configuration!");
            return;
        }
        System.out.println("INFO: Configuring columns for userTableManage...");
        setupColumnFactory(userIdColManage, "user_id");
        setupColumnFactory(userNameColManage, "full_name");
        setupColumnFactory(userAgeColManage, "age");
        setupColumnFactory(userPhoneColManage, "phone_number");
        setupColumnFactory(userEmailColManage, "email");
        setupColumnFactory(userAddressColManage, "address");
        setupColumnFactory(userGenderColManage, "gender");
        setupColumnFactory(userRoleColManage, "role");
        userTableManage.setPlaceholder(new Label("Đang tải dữ liệu..."));
    }

    private <S, T> void setupColumnFactory(TableColumn<S, T> column, String propertyName) {
        if (column != null) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        } else {
            System.err.println("WARN: TableColumn for property '" + propertyName + "' is null. Check FXML fx:id.");
        }
    }

    private void loadAndDisplayUserData() {
        System.out.println("INFO: Loading user data...");
        if (userTableManage == null) { return; }
        userTableManage.setPlaceholder(new Label("Đang tải dữ liệu..."));
        userTableManage.getItems().clear();

        // Tải trên luồng nền
        new Thread(() -> {
            ObservableList<User> users = FXCollections.observableArrayList();
            boolean loadError = false;
            String errorMessage = "Lỗi không xác định.";
            try {
                users = UserDAO.getAllUsers();
                this.allUsersList = users != null ? users : FXCollections.observableArrayList();
            } catch (Exception e) {
                loadError = true;
                errorMessage = "Lỗi không mong muốn khi tải người dùng.";
                System.err.println("ERROR: " + errorMessage); e.printStackTrace();
            }
            final ObservableList<User> finalUsers = this.allUsersList;
            final boolean finalLoadError = loadError;
            final String finalErrorMessage = errorMessage;
            Platform.runLater(() -> {
                if (userTableManage != null) {
                    if (!finalLoadError) {
                        userTableManage.setItems(finalUsers);
                        userTableManage.setPlaceholder(new Label(finalUsers.isEmpty() ? "Không có người dùng nào." : ""));
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ Liệu", finalErrorMessage);
                        userTableManage.setPlaceholder(new Label("Lỗi tải dữ liệu."));
                    }
                }
            });
        }).start();
    }

    // --- Các phương thức xử lý sự kiện nút trong Pane Quản lý User ---

    /**
     * Xử lý sự kiện khi nhập vào ô tìm kiếm và nhấn Enter, hoặc nhấn nút Tìm.
     */
    @FXML
    private void onSearchUserKeywordInput(ActionEvent event) {
        String keyword = searchUserField.getText();
        System.out.println("INFO: Searching users with keyword: " + keyword);
        filterUserData(keyword); // Gọi hàm lọc
    }

    /**
     * Lọc dữ liệu trên bảng userTableManage dựa trên allUsersList.
     */
    private void filterUserData(String keyword) {
        if (allUsersList == null) {
            System.out.println("WARN: allUsersList is null, cannot filter.");
            return;
        }

        ObservableList<User> filteredList;
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList = allUsersList; // Hiển thị lại toàn bộ nếu keyword trống
        } else {
            String lowerCaseKeyword = keyword.toLowerCase().trim();
            filteredList = allUsersList.stream()
                    .filter(user -> userMatchesKeyword(user, lowerCaseKeyword))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
        // Cập nhật bảng quản lý user với kết quả lọc
        if (userTableManage != null) {
            userTableManage.setItems(filteredList);
            userTableManage.setPlaceholder(new Label(filteredList.isEmpty() ? "Không tìm thấy người dùng phù hợp." : ""));
        } else {
            System.err.println("ERROR: userTableManage is null when trying to display search results.");
        }
    }

    /**
     * Hàm kiểm tra xem một User có khớp với từ khóa không.
     */
    private boolean userMatchesKeyword(User user, String lowerCaseKeyword) {
        if (user == null) return false;
        return String.valueOf(user.getUser_id()).contains(lowerCaseKeyword) ||
                (user.getFull_name() != null && user.getFull_name().toLowerCase().contains(lowerCaseKeyword)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseKeyword)) ||
                (user.getPhone_number() != null && user.getPhone_number().contains(lowerCaseKeyword)) ||
                (user.getGender() != null && user.getGender().toLowerCase().contains(lowerCaseKeyword)) ||
                (user.getRole() != null && user.getRole().toLowerCase().contains(lowerCaseKeyword));
    }


    /**
     * Xử lý sự kiện cho nút "Thêm mới" (Thực chất là đổi Role thành Admin).
     */
    @FXML
    private void onAddUserButtonClick(ActionEvent event) { // Đổi tên hàm cho rõ mục đích
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return; // Đã có alert trong getSelectedUser

        // Kiểm tra xem người dùng đã là Admin chưa
        if ("Admin".equalsIgnoreCase(selectedUser.getRole())) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Người dùng này đã là Admin.");
            return;
        }

        // Xác nhận hành động
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận");
        confirmation.setHeaderText("Nâng cấp thành Admin");
        confirmation.setContentText("Bạn có chắc muốn chuyển người dùng '" + selectedUser.getFull_name() + "' thành Admin không?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Thực hiện cập nhật Role trong luồng nền
            new Thread(() -> {
                boolean success = false;
                String message = "Lỗi không xác định khi cập nhật vai trò.";
                try {
                    success = UserDAO.updateUserRole(selectedUser.getUser_id(), "Admin");
                    if (success) {
                        message = "Đã chuyển người dùng '" + selectedUser.getFull_name() + "' thành Admin thành công.";
                    } else {
                        message = "Không thể cập nhật vai trò cho người dùng. Người dùng có thể không tồn tại hoặc có lỗi xảy ra.";
                    }
                } catch (SQLException e) {
                    message = "Lỗi SQL khi cập nhật vai trò: " + e.getMessage();
                    System.err.println("ERROR: SQL Error updating user role: " + e.getMessage());
                    e.printStackTrace();
                    success = false;
                }

                // Cập nhật UI
                final boolean finalSuccess = success;
                final String finalMessage = message;
                Platform.runLater(() -> {
                    showAlert(finalSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            finalSuccess ? "Thành công" : "Thất bại",
                            finalMessage);
                    if (finalSuccess) {
                        loadAndDisplayUserData(); // Load lại dữ liệu để thấy vai trò mới
                    }
                });
            }).start();
        }
    }

    /**
     * Xử lý sự kiện cho nút "Chỉnh sửa". (Để trống theo yêu cầu)
     */
    @FXML
    private void onEditUserButtonClick(ActionEvent event) {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return; // Đã có alert

        System.out.println("INFO: Edit user button clicked for user ID: " + selectedUser.getUser_id());
        showAlert(Alert.AlertType.INFORMATION,"Chức năng","Chức năng 'Chỉnh sửa' người dùng sẽ được làm sau.");
        // TODO: Mở cửa sổ/dialog sửa user
    }

    /**
     * Xử lý sự kiện cho nút "Xoá".
     */
    @FXML
    private void onDeleteUserButtonClick(ActionEvent event) {
        User selectedUser = getSelectedUser();
        if (selectedUser == null) return; // Đã có alert

        // Xác nhận xóa
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận xóa");
        confirmation.setHeaderText("Xóa người dùng: " + selectedUser.getFull_name() + " (ID: " + selectedUser.getUser_id() + ")");
        confirmation.setContentText("Bạn có chắc muốn xóa vĩnh viễn người dùng này không? Hành động này không thể hoàn tác.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Thực hiện xóa trong luồng nền
            new Thread(() -> {
                boolean deleted = false;
                String message = "Lỗi không xác định khi xóa người dùng.";
                try {
                    deleted = UserDAO.deleteUser(selectedUser.getUser_id());
                    if (deleted) {
                        message = "Đã xóa người dùng '" + selectedUser.getFull_name() + "' thành công.";
                    } else {
                        message = "Không thể xóa người dùng. Người dùng có thể không tồn tại hoặc có ràng buộc dữ liệu liên quan.";
                    }
                } catch (SQLException e) {
                    message = "Lỗi SQL khi xóa người dùng: " + e.getMessage();
                    System.err.println("ERROR: Lỗi SQL khi xóa user ID " + selectedUser.getUser_id() + ": " + e.getMessage());
                    e.printStackTrace();
                    deleted = false;
                }

                // Cập nhật UI
                final boolean finalDeleted = deleted;
                final String finalMessage = message;
                Platform.runLater(() -> {
                    showAlert(finalDeleted ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            finalDeleted ? "Thành công" : "Thất bại",
                            finalMessage);
                    if (finalDeleted) {
                        loadAndDisplayUserData(); // Load lại dữ liệu sau khi xóa thành công
                    }
                });
            }).start();
        }
    }

    /**
     * Hàm helper lấy người dùng đang được chọn trong bảng userTableManage.
     * Trả về null và hiển thị cảnh báo nếu không có ai được chọn.
     */
    private User getSelectedUser() {
        if (userTableManage == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Bảng người dùng chưa được khởi tạo.");
            return null;
        }
        User selectedUser = userTableManage.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một người dùng trong bảng trước.");
        }
        return selectedUser;
    }

    // --- Hàm showAlert tiện ích ---
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        // Đảm bảo chạy trên luồng UI
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

    public void onSearchFoodKeywordInput(ActionEvent actionEvent) {
    }

    public void onEditFoodButtonClick(ActionEvent actionEvent) {
    }

    public void onDeleteFoodButtonClick(ActionEvent actionEvent) {
    }
}