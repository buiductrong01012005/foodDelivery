package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.UserDAO;
// import com.example.fooddelivery.Dao.FoodDAO; // Cần khi có DAO
// import com.example.fooddelivery.Dao.OrderDAO; // Cần khi có DAO
import com.example.fooddelivery.Model.User;
// import com.example.fooddelivery.Model.Food;
import javafx.application.Platform;
import javafx.collections.FXCollections; // Thêm nếu cần tạo list rỗng
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;
// Import thêm cho LocalDate, YearMonth nếu dùng thống kê
// import java.time.LocalDate;
// import java.time.YearMonth;

public class AdminDashboardController implements Initializable {

    @FXML private AnchorPane dashboardPaneRoot;

    // --- Khai báo @FXML cho các controls trong DashboardView.fxml ---
    @FXML private Label dailyRevenueLabel;
    @FXML private Label monthlyRevenueLabel;
    @FXML private Label totalFoodLabel;
    @FXML private Label availableFoodLabel;

    @FXML private TableView<User> userTableDashboard;
    @FXML private TableColumn<User, Integer> userIdColDashboard;
    @FXML private TableColumn<User, String> userNameColDashboard;
    @FXML private TableColumn<User, String> userEmailColDashboard;
    @FXML private TableColumn<User, String> userRoleColDashboard;
    @FXML private TableColumn<User, String> userPhoneColDashboard;
    // @FXML private TableColumn<User, Integer> userAgeColDashboard; // Bỏ cột này nếu không có trong FXML

    @FXML private TableView<?> foodTableDashboard; // Sử dụng wildcard '?' nếu chưa có Model Food
    @FXML private TableColumn<?, ?> foodIdColDashboard;
    @FXML private TableColumn<?, ?> foodNameColDashboard;
    @FXML private TableColumn<?, ?> foodStatusColDashboard;

    // private AdminContainerController adminContainerController; // Thêm nếu cần gọi ngược lại container
    private boolean initialDataLoaded = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminDashboardController initialized.");
        configureTables();
        // Dữ liệu sẽ được load bởi loadDataIfNeeded khi pane hiển thị
    }

    // Inject container nếu cần
    // public void setAdminContainerController(AdminContainerController controller) { this.adminContainerController = controller; }

    private void configureTables() {
        System.out.println("AdminDashboardController: Configuring tables...");
        if (userTableDashboard != null) {
            // Sử dụng PropertyValueFactory để liên kết cột với thuộc tính của User Model
            setupColumnFactory(userIdColDashboard, "user_id"); // Tên thuộc tính trong User.java
            setupColumnFactory(userNameColDashboard, "full_name");
            setupColumnFactory(userEmailColDashboard, "email");
            setupColumnFactory(userRoleColDashboard, "role");
            setupColumnFactory(userPhoneColDashboard, "phone_number");
            // setupColumnFactory(userAgeColDashboard, "age"); // Nếu có cột tuổi và hàm getAge() trong User
            userTableDashboard.setPlaceholder(new Label("Chưa có dữ liệu..."));
        } else {
            System.err.println("AdminDashboardController Error: userTableDashboard is null!");
        }

        if (foodTableDashboard != null) {
            // TODO: Cấu hình cột cho bảng food khi có Model Food
            // Ví dụ: setupColumnFactory(foodIdColDashboard, "foodId");
            //        setupColumnFactory(foodNameColDashboard, "name");
            //        setupColumnFactory(foodStatusColDashboard, "status");
            foodTableDashboard.setPlaceholder(new Label("Chưa có dữ liệu..."));
        } else {
            System.err.println("AdminDashboardController Error: foodTableDashboard is null!");
        }
    }

    // Helper setup cột
    private <S, T> void setupColumnFactory(TableColumn<S, T> column, String propertyName) {
        if (column != null && propertyName != null && !propertyName.isEmpty()) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        } else {
            System.err.println("WARN (AdminDashboard): Invalid column or propertyName for setupColumnFactory. Column: " + column + ", Property: " + propertyName);
        }
    }

    // Được gọi bởi AdminContainerController
    public void loadDataIfNeeded() {
        if (!initialDataLoaded) {
            System.out.println("AdminDashboardController: Loading initial data...");
            refreshData(); // Gọi hàm tải/làm mới dữ liệu
            initialDataLoaded = true;
        } else {
            System.out.println("AdminDashboardController: Data already loaded. Maybe refresh?");
            // Tùy chọn: Gọi refreshData() ở đây nếu muốn cập nhật mỗi khi quay lại tab
            // refreshData();
        }
    }

    // Hàm tải/làm mới tất cả dữ liệu cho dashboard
    public void refreshData() {
        System.out.println("AdminDashboardController: Refreshing all dashboard data...");
        loadUserDashboardData();
        loadFoodDashboardData();
        loadStatsData();
    }

    // Tải dữ liệu User cho bảng dashboard
    private void loadUserDashboardData() {
        if (userTableDashboard == null) return;
        userTableDashboard.setPlaceholder(new Label("Đang tải người dùng..."));
        new Thread(() -> {
            try {
                // Lấy danh sách user (có thể giới hạn số lượng bằng SQL nếu cần)
                ObservableList<User> users = UserDAO.getAllUsers(); // Tạm lấy hết
                // TODO: Giới hạn số lượng users hiển thị trên dashboard nếu cần
                // ví dụ: users = FXCollections.observableArrayList(users.subList(0, Math.min(users.size(), 10)));

                Platform.runLater(() -> {
                    if (userTableDashboard != null) {
                        userTableDashboard.setItems(users);
                        userTableDashboard.setPlaceholder(new Label(users == null || users.isEmpty() ? "Không có người dùng." : ""));
                        System.out.println("AdminDashboardController: User data loaded/refreshed.");
                    }
                });
            } catch (Exception e) {
                System.err.println("AdminDashboardController Error loading users: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (userTableDashboard != null) userTableDashboard.setPlaceholder(new Label("Lỗi tải người dùng."));
                });
            }
        }).start();
    }

    // Tải dữ liệu Food cho bảng dashboard
    private void loadFoodDashboardData() {
        if (foodTableDashboard == null) return;
        foodTableDashboard.setPlaceholder(new Label("Đang tải món ăn..."));
        // TODO: Triển khai FoodDAO và logic lấy món sắp hết hàng
        System.out.println("AdminDashboardController: loadFoodDashboardData() - Placeholder");
        Platform.runLater(()->{
            foodTableDashboard.setPlaceholder(new Label("Chức năng đang phát triển."));
            foodTableDashboard.setItems(FXCollections.emptyObservableList()); // Xóa dữ liệu cũ
        });
    }

    // Tải dữ liệu thống kê
    private void loadStatsData() {
        // TODO: Triển khai DAO để lấy doanh thu, số lượng món...
        System.out.println("AdminDashboardController: loadStatsData() - Placeholder");
        Platform.runLater(()->{
            // Gán giá trị mặc định hoặc "Đang tải..."
            if(dailyRevenueLabel!=null) dailyRevenueLabel.setText("N/A");
            if(monthlyRevenueLabel!=null) monthlyRevenueLabel.setText("N/A");
            if(totalFoodLabel!=null) totalFoodLabel.setText("N/A");
            if(availableFoodLabel!=null) availableFoodLabel.setText("N/A");
        });
    }

    // Hàm tiện ích hiển thị Alert
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}