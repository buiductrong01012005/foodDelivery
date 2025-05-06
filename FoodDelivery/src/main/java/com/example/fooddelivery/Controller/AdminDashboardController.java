package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.FoodDAO; // Cần FoodDAO
import com.example.fooddelivery.Dao.UserDAO;
import com.example.fooddelivery.Model.Food; // Cần Model Food
import com.example.fooddelivery.Model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());

    // --- FXML Components from AdminDashboardContent.fxml ---
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
    @FXML private TableView<Food> foodTableDashboard;
    @FXML private TableColumn<Food, Integer> foodIdColDashboard;
    @FXML private TableColumn<Food, String> foodNameColDashboard;
    @FXML private TableColumn<Food, String> foodStatusColDashboard;

    // Data lists specific to this dashboard
    private ObservableList<User> recentUsersList = FXCollections.observableArrayList();
    private ObservableList<Food> lowStockFoodsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initializing AdminDashboardController...");

        // Configure table columns
        configureUserTable();
        configureFoodTable();

        // Load data asynchronously
        loadDashboardData();
    }

    /**
     * Cấu hình cột cho bảng User trên Dashboard.
     */
    private void configureUserTable() {
        if (userTableDashboard == null) return;
        setupColumnFactory(userIdColDashboard, "user_id");
        setupColumnFactory(userNameColDashboard, "full_name");
        setupColumnFactory(userEmailColDashboard, "email");
        setupColumnFactory(userRoleColDashboard, "role");
        setupColumnFactory(userPhoneColDashboard, "phone_number");
        userTableDashboard.setPlaceholder(new Label("Đang tải người dùng..."));
    }

    /**
     * Cấu hình cột cho bảng Food trên Dashboard.
     */
    private void configureFoodTable() {
        if (foodTableDashboard == null) return;
        setupColumnFactory(foodIdColDashboard, "food_id");
        setupColumnFactory(foodNameColDashboard, "name");
        setupColumnFactory(foodStatusColDashboard, "availability_status");
        foodTableDashboard.setPlaceholder(new Label("Đang tải món ăn..."));
    }

    /**
     * Hàm helper thiết lập CellValueFactory an toàn.
     */
    private <S, T> void setupColumnFactory(TableColumn<S, T> column, String propertyName) {
        if (column != null) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        } else {
            LOGGER.warning("TableColumn for property '" + propertyName + "' is null. Check FXML fx:id.");
        }
    }

    /**
     * Tải tất cả dữ liệu cần thiết cho Dashboard trên luồng nền.
     */
    void loadDashboardData() {
        new Thread(() -> {
            LOGGER.info("Background: Loading dashboard data...");
            ObservableList<User> users = FXCollections.observableArrayList();
            ObservableList<Food> foods = FXCollections.observableArrayList();
            // Giả sử có các biến để lưu thống kê
            String dailyRev = "[Lỗi]";
            String monthlyRev = "[Lỗi]";
            long totalFoods = 0;
            long availableFoods = 0;
            boolean userError = false;
            boolean foodError = false;

            try {
                users = UserDAO.getAllUsers();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (users == null) users = FXCollections.observableArrayList();

            try {
                foods = FoodDAO.getAllFoods();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (foods == null) foods = FXCollections.observableArrayList();


            ObservableList<Food> allFoodsForCount = null;
            try {
                allFoodsForCount = FoodDAO.getAllFoods();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (allFoodsForCount != null) {
                totalFoods = allFoodsForCount.size();
                availableFoods = allFoodsForCount.stream()
                        .filter(f -> "Available".equalsIgnoreCase(f.getAvailability_status()))
                        .count();
            }

//            // Lọc lại danh sách food chỉ hiển thị món hết hàng (nếu cần)
//            ObservableList<Food> unavailableFoods = foods.stream()
//                    .filter(f -> "Unavailable".equalsIgnoreCase(f.getAvailability_status()))
//                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
//            foods = unavailableFoods; // Gán lại để hiển thị món hết hàng

            try {
                // TODO: Gọi DAO để lấy doanh thu thực tế
//                 dailyRev = formatCurrency(StatisticsDAO.getTodayRevenue());
//                 monthlyRev = formatCurrency(StatisticsDAO.getCurrentMonthRevenue());
                dailyRev = "5,500,000 VND"; // Dữ liệu giả
                monthlyRev = "150,000,000 VND"; // Dữ liệu giả

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Background: Error loading revenue stats", e);

            }


            // Chuẩn bị dữ liệu cuối cùng để cập nhật UI
            final ObservableList<User> finalUsers = users;
            final ObservableList<Food> finalFoods = foods; // List đã lọc (nếu có)
            final boolean finalUserError = userError;
            final boolean finalFoodError = foodError;
            final String finalDailyRev = dailyRev;
            final String finalMonthlyRev = monthlyRev;
            final long finalTotalFoods = totalFoods;
            final long finalAvailableFoods = availableFoods;

            // Cập nhật UI trên luồng chính
            Platform.runLater(() -> {
                updateDashboardUI(finalUsers, finalFoods, finalUserError, finalFoodError,
                        finalDailyRev, finalMonthlyRev, finalTotalFoods, finalAvailableFoods);
            });

        }).start();
    }

    /**
     * Cập nhật các thành phần UI của Dashboard với dữ liệu đã tải.
     */
    private void updateDashboardUI(ObservableList<User> users, ObservableList<Food> foods,
                                   boolean userError, boolean foodError,
                                   String dailyRev, String monthlyRev, long totalFoods, long availableFoods) {
        LOGGER.info("Updating Dashboard UI...");

        // Cập nhật bảng User
        if (userTableDashboard != null) {
            if (!userError) {
                userTableDashboard.setItems(users);
                userTableDashboard.setPlaceholder(new Label(users.isEmpty() ? "Không có người dùng gần đây." : ""));
            } else {
                userTableDashboard.getItems().clear();
                userTableDashboard.setPlaceholder(new Label("Lỗi tải dữ liệu người dùng."));
            }
        }

        // Cập nhật bảng Food
        if (foodTableDashboard != null) {
            if (!foodError) {
                foodTableDashboard.setItems(foods);
                foodTableDashboard.setPlaceholder(new Label(foods.isEmpty() ? "Không có món ăn sắp hết hàng." : ""));
            } else {
                foodTableDashboard.getItems().clear();
                foodTableDashboard.setPlaceholder(new Label("Lỗi tải dữ liệu món ăn."));
            }
        }

        // Cập nhật các Label thống kê
        setTextSafe(dailyRevenueLabel, dailyRev);
        setTextSafe(monthlyRevenueLabel, monthlyRev);
        setTextSafe(totalFoodLabel, String.valueOf(totalFoods));
        setTextSafe(availableFoodLabel, String.valueOf(availableFoods));
    }


    // --- Hàm helper setTextSafe và showAlert ---
    private void setTextSafe(Label label, String text) {
        if (label != null) {
            label.setText(text != null ? text : "[N/A]");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

}