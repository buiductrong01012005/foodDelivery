package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Main;
import com.example.fooddelivery.Model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane; // Hoặc kiểu Pane gốc của view con
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminContainerController implements Initializable {

    @FXML private StackPane contentStackPane;

    // --- Khai báo @FXML cho các Node gốc của view con (từ fx:id của <fx:include>) ---
    @FXML private AnchorPane dashboardView;    // Tên khớp fx:id trong FXML
    @FXML private AnchorPane manageUsersView;
    @FXML private AnchorPane manageFoodView;
    @FXML private AnchorPane userInfoView;     // fx:id của include cho UserInfoView.fxml

    // --- Khai báo @FXML cho các Controller con (Tên = fx:id + "Controller") ---
    @FXML private AdminDashboardController dashboardViewController; // Đã đổi tên
    @FXML private AdminManageUsersController manageUsersViewController; // Đã đổi tên
    @FXML private AdminManageFoodController manageFoodViewController;   // Đã đổi tên
    @FXML private InforController userInfoViewController; // Đã đổi tên (cho userInfoView)

    // --- Khai báo @FXML cho các nút Sidebar ---
    @FXML private Button dashboardBtn, foodBtn, orderBtn, userBtn, logOutBtn;
    // Nút inforBtn đã bị xóa khỏi FXML và controller này

    // --- Thông tin chung ---
    private User currentAdminUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminContainerController initialized.");

        // Inject tham chiếu của container này vào các controller con
        if (manageUsersViewController != null) {
            manageUsersViewController.setAdminContainerController(this);
        }
        if (userInfoViewController != null) {
            userInfoViewController.setAdminContainerController(this);
        }
        if (manageFoodViewController != null) {
            manageFoodViewController.setAdminContainerController(this); // Nếu cần
        }
        if (dashboardViewController != null) {
            // dashboardViewController.setAdminContainerController(this); // Nếu cần
        }

        // Hiển thị Dashboard mặc định khi khởi tạo
        showPane(dashboardView);
        // Controller con sẽ tự load data khi được hiển thị lần đầu qua showPane
    }

    // --- Setter cho Admin User (sẽ được gọi từ LoginController) ---
    public void setCurrentAdminUser(User adminUser) {
        this.currentAdminUser = adminUser;
        String adminEmail = (adminUser != null ? adminUser.getEmail() : "null");
        System.out.println("AdminContainerController: Current admin set: " + adminEmail);

        // Truyền thông tin admin xuống các controller con cần nó
        if (manageUsersViewController != null) {
            manageUsersViewController.setCurrentAdminUser(adminUser);
        }
        if (userInfoViewController != null) {
            userInfoViewController.setCurrentAdminUser(adminUser);
        }
        // Truyền cho các controller khác nếu cần
    }

    // --- Hàm quản lý hiển thị Pane con ---
    private void showPane(Node paneToShow) {
        if (contentStackPane == null || paneToShow == null) {
            System.err.println("AdminContainerController Error: Cannot show pane - StackPane or target pane is null.");
            return;
        }
        contentStackPane.getChildren().forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });
        paneToShow.setVisible(true);
        paneToShow.setManaged(true);
        System.out.println("AdminContainerController: Showing pane with fx:id = " + paneToShow.getId());

        // Kích hoạt load data lần đầu cho pane vừa hiển thị
        if (paneToShow.getId() != null) {
            switch (paneToShow.getId()) {
                case "dashboardView":
                    if (dashboardViewController != null) dashboardViewController.loadDataIfNeeded();
                    break;
                case "manageUsersView":
                    if (manageUsersViewController != null) manageUsersViewController.loadDataIfNeeded();
                    break;
                case "manageFoodView":
                    if (manageFoodViewController != null) manageFoodViewController.loadDataIfNeeded();
                    break;
                // Không cần load gì cho userInfoView ở đây, nó được load khi có request
            }
        }
    }

    // --- Xử lý sự kiện Sidebar ---
    @FXML void showDashboard(ActionEvent event) { showPane(dashboardView); }
    @FXML void showManageFood(ActionEvent event) { showPane(manageFoodView); }
    @FXML void showManageOrders(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng Quản lý Đơn hàng đang được phát triển.");
    }
    @FXML void showManageUsers(ActionEvent event) { showPane(manageUsersView); }
    // Hàm showAdminInfo đã bị xóa

    @FXML
    void handleLogout(ActionEvent event) {
        System.out.println("AdminContainerController: Logging out.");
        this.currentAdminUser = null;
        // Có thể thêm logic reset trạng thái các controller con nếu cần
        Main.showLoginView();
    }

    // --- Hàm điều phối (được gọi bởi Controller con) ---

    /** Được gọi bởi AdminManageUsersController khi nút "Chi tiết" được nhấn. */
    public void requestUserView(User userToView) {
        if (userToView != null && userInfoViewController != null) {
            System.out.println("AdminContainerController: Request VIEW user: " + userToView.getEmail());
            userInfoViewController.displayUserInfo(userToView, false); // false = chế độ xem
            showPane(userInfoView);
        } else { handleCoordinatorError("Cannot process VIEW request - User or InforController is null."); }
    }

    /** Được gọi bởi AdminManageUsersController khi nút "Chỉnh sửa" được nhấn. */
    public void requestUserEdit(User userToEdit) {
        if (userToEdit != null && userInfoViewController != null) {
            System.out.println("AdminContainerController: Request EDIT user: " + userToEdit.getEmail());
            userInfoViewController.displayUserInfo(userToEdit, true); // true = chế độ sửa
            showPane(userInfoView);
        } else { handleCoordinatorError("Cannot process EDIT request - User or InforController is null."); }
    }

    /** Được gọi bởi InforController sau khi Hủy hoặc Lưu thành công. */
    public void finishedUserInfoAction() { // Đổi tên hàm cho rõ ràng hơn
        System.out.println("AdminContainerController: Finished user info action, returning to Manage Users view.");
        showPane(manageUsersView); // Quay lại màn hình quản lý user
        // Yêu cầu bảng user làm mới để thấy thay đổi
        if (manageUsersViewController != null) {
            manageUsersViewController.refreshUsersTable();
        }
    }

    // --- Hàm tiện ích ---
    private void handleCoordinatorError(String logMessage) {
        System.err.println("AdminContainerController Error: " + logMessage);
        showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi điều phối. Vui lòng thử lại.");
    }

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