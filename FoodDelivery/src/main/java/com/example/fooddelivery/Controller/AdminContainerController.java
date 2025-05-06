package com.example.fooddelivery.Controller;
import com.example.fooddelivery.Main;
import com.example.fooddelivery.Model.Food;
import com.example.fooddelivery.Model.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.ResourceBundle;
public class AdminContainerController implements Initializable {
    @FXML
    private StackPane contentStackPane;

    // --- Views (Nodes from <fx:include>) ---
    @FXML
    private AnchorPane dashboardView;
    @FXML
    private AnchorPane manageUsersView;
    @FXML
    private AnchorPane manageFoodView;
    @FXML
    private AnchorPane userInfoView;
    @FXML
    private AnchorPane foodInfoView;
    @FXML
    private AnchorPane manageOrderView;

    // --- Child Controllers ---
    @FXML
    private AdminDashboardController dashboardViewController;
    @FXML
    private AdminManageUsersController manageUsersViewController;
    @FXML
    private AdminManageFoodController manageFoodViewController;
    @FXML
    private InforController userInfoViewController;
    @FXML
    private FoodInforController foodInfoViewController;
    @FXML
    private AdminManageOrderController manageOrderViewController;

    @FXML
    private Button dashboardBtn, foodBtn, orderBtn, userBtn, logOutBtn;
    private User currentAdminUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (userInfoViewController != null) {
            userInfoViewController.setAdminContainerController(this);
        }
        System.out.println("AdminContainerController initialized.");

        if (manageUsersViewController != null) {
            manageUsersViewController.setAdminContainerController(this);
        }

        if (manageFoodViewController != null) {
            manageFoodViewController.setAdminContainerController(this);
        }
        showPane(dashboardView);
    }

    public void setCurrentAdminUser(User adminUser) {
        this.currentAdminUser = adminUser;
        String adminInfo = (adminUser != null ? adminUser.getEmail() + " (ID: " + adminUser.getUser_id() + ")" : "null");
        System.out.println("AdminContainerController: Current admin set: " + adminInfo);

        if (manageUsersViewController != null) {
            manageUsersViewController.setCurrentAdminUser(adminUser);
        }
        if (userInfoViewController != null && adminUser != null) {
            userInfoViewController.setCurrentAdminId(adminUser.getUser_id());
        }
        if (foodInfoViewController != null && adminUser != null) {
            foodInfoViewController.setCurrentAdminId(adminUser.getUser_id());
        }
    }

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

        if (paneToShow.getId() != null) {
            switch (paneToShow.getId()) {
                case "dashboardView":
                    if (dashboardViewController != null) dashboardViewController.loadDashboardData();
                    break;
                case "manageUsersView":
                    if (manageUsersViewController != null) manageUsersViewController.loadDataIfNeeded();
                    break;
                case "manageFoodView":
                    if (manageFoodViewController != null) manageFoodViewController.loadAndDisplayFoodData();
                    break;
                case "manageOrderView":
                    if (manageOrderViewController != null) manageOrderViewController.loadOrderData();
            }
        }
    }

    @FXML
    void showDashboard(ActionEvent event) {
        showPane(dashboardView);
    }

    @FXML
    void showManageFood(ActionEvent event) {
        showPane(manageFoodView);
    }

    @FXML
    void showManageOrders(ActionEvent event) {
        showPane(manageOrderView);
    }

    @FXML
    void showManageUsers(ActionEvent event) {
        showPane(manageUsersView);
    }

    @FXML
    void handleLogout(ActionEvent event) { /* ... */ }

    // --- User Information Coordination
    public void requestUserView(User userToView) {
        if (userToView != null && userInfoViewController != null && userInfoView != null) {
            System.out.println("AdminContainerController: Request VIEW user: " + userToView.getEmail());
            if (currentAdminUser != null) {
                userInfoViewController.setCurrentAdminId(currentAdminUser.getUser_id()); // Quan trọng
                userInfoViewController.displayUserInfo(userToView, false);
                showPane(userInfoView);
            }
        } else {
            handleCoordinatorError("Cannot process USER VIEW request - User or InforController/View is null.");
        }
    }

    public void requestUserEdit(User userToEdit) {
        if (userToEdit != null && userInfoViewController != null && userInfoView != null) {
            System.out.println("AdminContainerController: Request EDIT user: " + userToEdit.getEmail());
            if (currentAdminUser != null) {
                userInfoViewController.setCurrentAdminId(currentAdminUser.getUser_id()); // Quan trọng
                userInfoViewController.displayUserInfo(userToEdit, true);
                showPane(userInfoView);
            }
        } else {
            handleCoordinatorError("Cannot process USER EDIT request - User or InforController/View is null.");
        }
    }

    private void handleUserInfoCompletion(boolean dataWasSaved) {
        System.out.println("AdminContainerController: Finished user info action, Data saved: " + dataWasSaved);
        showPane(manageUsersView);
        if (dataWasSaved && manageUsersViewController != null) {
            manageUsersViewController.refreshUsersTable();
        }
    }


    /**
     * Được gọi bởi AdminManageFoodController để hiển thị chi tiết món ăn.
     */
    public void requestFoodView(Food foodToView) {
        if (foodToView != null && foodInfoViewController != null && foodInfoView != null) {
            System.out.println("AdminContainerController: Request VIEW food: " + foodToView.getName());
            if (currentAdminUser != null) foodInfoViewController.setCurrentAdminId(currentAdminUser.getUser_id());
            foodInfoViewController.loadFoodInformation(foodToView, FoodInforController.FormMode.VIEW, this::handleFoodInfoCompletion);
            showPane(foodInfoView);
        } else {
            handleCoordinatorError("Cannot process FOOD VIEW request - components or food item missing.");
        }
    }

    /**
     * Được gọi bởi AdminManageFoodController để hiển thị form chỉnh sửa món ăn.
     */
    public void requestFoodEdit(Food foodToEdit) {
        if (foodToEdit != null && foodInfoViewController != null && foodInfoView != null) {
            System.out.println("AdminContainerController: Request EDIT food: " + foodToEdit.getName());
            if (currentAdminUser == null) { // Admin phải đăng nhập để biết ai là người cập nhật
                handleCoordinatorError("Admin user not set. Cannot proceed with food edit.");
                return;
            }
            foodInfoViewController.setCurrentAdminId(currentAdminUser.getUser_id());
            foodInfoViewController.loadFoodInformation(foodToEdit, FoodInforController.FormMode.EDIT, this::handleFoodInfoCompletion);
            showPane(foodInfoView);
        } else {
            handleCoordinatorError("Cannot process FOOD EDIT request - components or food item missing.");
        }
    }

    /**
     * Được gọi bởi AdminManageFoodController để hiển thị form thêm món ăn mới.
     */
    public void requestFoodAdd() {
        if (foodInfoViewController != null && foodInfoView != null) {
            System.out.println("AdminContainerController: Request ADD new food.");
            if (currentAdminUser == null) { // Admin phải đăng nhập để biết ai là người tạo
                handleCoordinatorError("Admin user not set. Cannot proceed with adding food.");
                return;
            }
            foodInfoViewController.setCurrentAdminId(currentAdminUser.getUser_id());
            foodInfoViewController.loadFoodInformation(null, FoodInforController.FormMode.ADD, this::handleFoodInfoCompletion);
            showPane(foodInfoView);
        } else {
            handleCoordinatorError("Cannot process FOOD ADD request - components missing.");
        }
    }

    /**
     * Callback được gọi bởi FoodInforController khi hoàn thành hành động.
     */
    private void handleFoodInfoCompletion(boolean dataWasChanged) {
        System.out.println("AdminContainerController: Food info action finished. Data changed: " + dataWasChanged);
        showPane(manageFoodView); // Quay lại màn hình quản lý món ăn
        if (dataWasChanged && manageFoodViewController != null) {
            manageFoodViewController.loadAndDisplayFoodData(); // Làm mới bảng món ăn
        }
    }


    private void handleCoordinatorError(String logMessage) { /* ... */ }

    private void showAlert(Alert.AlertType alertType, String title, String content) { /* ... */ }

    private void showAlertInternal(Alert.AlertType alertType, String title, String content) { /* ... */ }

    public void finishedUserInfoAction() {;
        showPane(manageUsersView);
        if (manageUsersView != null) {
            manageUsersViewController.loadAndDisplayUserData();
        }
    }
}