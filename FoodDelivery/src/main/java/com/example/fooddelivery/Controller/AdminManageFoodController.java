package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.FoodDAO; // Import FoodDAO
import com.example.fooddelivery.Model.Food; // Import Food Model
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
// import java.util.logging.Level; // Không dùng Logger
// import java.util.logging.Logger; // Không dùng Logger
import java.util.stream.Collectors;

/**
 * Controller quản lý giao diện và logic cho màn hình Quản lý Món ăn (AdminManageFoodContent.fxml).
 * Phiên bản không sử dụng Logger, thay bằng System.out/err.
 */
public class AdminManageFoodController implements Initializable {

    // private static final Logger LOGGER = Logger.getLogger(AdminManageFoodController.class.getName()); // Bỏ Logger

    // --- FXML Components from AdminManageFoodContent.fxml ---
    @FXML private TextField searchFoodField;
    @FXML private TableView<Food> foodTableManage;
    @FXML private TableColumn<Food, Integer> foodIdColManage;
    @FXML private TableColumn<Food, String> foodNameColManage;
    @FXML private TableColumn<Food, String> foodCategoryColManage;
    @FXML private TableColumn<Food, Double> foodPriceColManage;
    @FXML private TableColumn<Food, String> foodStatusColManage;
    @FXML private Button addFoodButton;
    @FXML private Button editFoodButton;
    @FXML private Button deleteFoodButton;

    // --- Data ---
    private ObservableList<Food> allFoodsList = FXCollections.observableArrayList(); // Danh sách đầy đủ để lọc
    private Object adminContainerController;

    // Không cần DatabaseConnector ở đây nếu DAO dùng static connectDB()

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing AdminManageFoodController..."); // Thay Logger

        // Configure table columns
        configureFoodTableColumns();

        // Load initial food data
        loadAndDisplayFoodData();

        // Setup listener for search field (optional: trigger on text change)
        if (searchFoodField != null) {
            searchFoodField.textProperty().addListener((obs, oldVal, newVal) -> filterFoodData(newVal));
        }
    }

    /**
     * Cấu hình CellValueFactory cho các cột trong bảng foodTableManage.
     */
    private void configureFoodTableColumns() {
        if (foodTableManage == null) {
            System.err.println("ERROR: foodTableManage is null! Check FXML fx:id."); // Thay Logger
            return;
        }
        System.out.println("INFO: Configuring columns for foodTableManage..."); // Thay Logger
        setupColumnFactory(foodIdColManage, "food_id");
        setupColumnFactory(foodNameColManage, "name");
        setupColumnFactory(foodCategoryColManage, "category_name");
        setupColumnFactory(foodPriceColManage, "price");
        setupColumnFactory(foodStatusColManage, "availability_status");
        foodTableManage.setPlaceholder(new Label("Đang tải dữ liệu món ăn..."));
    }

    /**
     * Hàm helper thiết lập CellValueFactory an toàn.
     */
    private <S, T> void setupColumnFactory(TableColumn<S, T> column, String propertyName) {
        if (column != null) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        } else {
            System.err.println("WARN: TableColumn for property '" + propertyName + "' is null. Check FXML fx:id."); // Thay Logger
        }
    }

    /**
     * Tải dữ liệu món ăn từ DAO và hiển thị lên foodTableManage.
     * Thực hiện trên luồng nền.
     */
    void loadAndDisplayFoodData() {
        System.out.println("INFO: Initiating food data loading..."); // Thay Logger
        if (foodTableManage == null) {
            System.err.println("ERROR: foodTableManage is null. Cannot load data."); // Thay Logger
            return;
        }
        foodTableManage.setPlaceholder(new Label("Đang tải dữ liệu..."));
        // Xóa dữ liệu cũ trước khi bắt đầu tải trên luồng nền để tránh nhấp nháy
        foodTableManage.getItems().clear();

        new Thread(() -> {
            ObservableList<Food> foods = FXCollections.observableArrayList();
            boolean loadError = false;
            String errorMessage = "Lỗi không xác định khi tải dữ liệu món ăn.";

            try {
                foods = FoodDAO.getAllFoods();
                this.allFoodsList = foods != null ? foods : FXCollections.observableArrayList();
                System.out.println("INFO: DAO returned " + this.allFoodsList.size() + " foods."); // Thay Logger

            } catch (Exception e) {
                loadError = true;
                errorMessage = "Đã xảy ra lỗi không mong muốn khi tải dữ liệu món ăn.";
                System.err.println("ERROR: Unexpected error loading food data: " + e.getMessage()); // Thay Logger
                e.printStackTrace();
            }

            final ObservableList<Food> finalFoods = this.allFoodsList;
            final boolean finalLoadError = loadError;
            final String finalErrorMessage = errorMessage;

            // Cập nhật TableView trên luồng JavaFX
            Platform.runLater(() -> {
                if (foodTableManage != null) {
                    if (!finalLoadError) {
                        foodTableManage.setItems(finalFoods);
                        foodTableManage.setPlaceholder(new Label(finalFoods.isEmpty() ? "Không có món ăn nào." : ""));
                        System.out.println("INFO: Food table updated."); // Thay Logger
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ Liệu", finalErrorMessage);
                        // Không cần clear items lần nữa vì đã clear ở đầu
                        foodTableManage.setPlaceholder(new Label("Lỗi tải dữ liệu món ăn."));
                    }
                } else {
                    System.err.println("ERROR: foodTableManage became null before displaying data?"); // Thay Logger
                }
            });

        }).start();
    }


    /**
     * Xử lý sự kiện cho nút Tìm kiếm (onAction của TextField hoặc Button).
     */
    @FXML
    private void onSearchFoodKeywordInput(ActionEvent event) {
        filterFoodData(searchFoodField.getText());
    }

    /**
     * Lọc dữ liệu trên bảng foodTableManage dựa trên allFoodsList.
     */
    private void filterFoodData(String keyword) {
        if (allFoodsList == null) {
            System.out.println("WARN: allFoodsList is null, cannot filter."); // Thay Logger
            return;
        };
        System.out.println("INFO: Filtering food with keyword: " + keyword); // Thay Logger

        ObservableList<Food> filteredList;
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList = allFoodsList;
        } else {
            String lowerCaseKeyword = keyword.toLowerCase().trim();
            filteredList = allFoodsList.stream()
                    .filter(food -> food != null && food.getName() != null &&
                            food.getName().toLowerCase().contains(lowerCaseKeyword))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }

        if (foodTableManage != null) {
            foodTableManage.setItems(filteredList);
            foodTableManage.setPlaceholder(new Label(filteredList.isEmpty() ? "Không tìm thấy món ăn phù hợp." : ""));
        } else {
            System.err.println("ERROR: foodTableManage is null when trying to display search results."); // Thay Logger
        }
    }


    /**
     * Xử lý sự kiện cho nút "Thêm mới" món ăn.
     */
    @FXML
    private void onAddFoodButtonClick(ActionEvent event) {
        System.out.println("INFO: Add new food button clicked."); // Thay Logger
        // TODO: Mở cửa sổ/dialog để nhập thông tin món ăn mới
        showAlert(Alert.AlertType.INFORMATION,"Chức năng","Mở form thêm món ăn mới.");
        // Sau khi thêm thành công: loadAndDisplayFoodData();
    }

    /**
     * Xử lý sự kiện cho nút "Chỉnh sửa" món ăn.
     */
    @FXML
    private void onEditFoodButtonClick(ActionEvent event) {
        Food selectedFood = getSelectedFood();
        if (selectedFood == null) return;

        System.out.println("INFO: Edit food button clicked for food ID: " + selectedFood.getFood_id()); // Thay Logger
        // TODO: Mở cửa sổ/dialog để sửa thông tin món ăn đã chọn
        showAlert(Alert.AlertType.INFORMATION,"Chức năng","Mở form sửa món ăn: " + selectedFood.getName());
        // Sau khi sửa thành công: loadAndDisplayFoodData();
    }

    /**
     * Xử lý sự kiện cho nút "Xoá" món ăn.
     */
    @FXML
    private void onDeleteFoodButtonClick(ActionEvent event) {
        Food selectedFood = getSelectedFood();
        if (selectedFood == null) return;

        System.out.println("INFO: Delete food button clicked for food ID: " + selectedFood.getFood_id()); // Thay Logger

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận xóa");
        confirmation.setHeaderText("Xóa món ăn: " + selectedFood.getName());
        confirmation.setContentText("Bạn có chắc muốn xóa món ăn này không?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean deleted = false;
                String message = "Lỗi không xác định khi xóa món ăn.";
                try {
                    deleted = FoodDAO.deleteFood(selectedFood.getFood_id()); // Giả sử DAO có hàm này
                    if (deleted) {
                        message = "Đã xóa món ăn '" + selectedFood.getName() + "' thành công.";
                    } else {
                        message = "Không thể xóa món ăn.";
                    }
                } catch (SQLException e) {
                    message = "Lỗi SQL khi xóa món ăn: " + e.getMessage();
                    System.err.println("ERROR: Lỗi SQL khi xóa food ID " + selectedFood.getFood_id()+ ": " + e.getMessage()); // Thay Logger
                    e.printStackTrace();
                    deleted = false;
                } catch (Exception e) {
                    message = "Lỗi không mong muốn khi xóa món ăn.";
                    System.err.println("ERROR: Lỗi không xác định khi xóa food ID " + selectedFood.getFood_id()+ ": " + e.getMessage()); // Thay Logger
                    e.printStackTrace();
                    deleted = false;
                }

                final boolean finalDeleted = deleted;
                final String finalMessage = message;
                Platform.runLater(() -> {
                    showAlert(finalDeleted ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            finalDeleted ? "Thành công" : "Thất bại",
                            finalMessage);
                    if (finalDeleted) {
                        loadAndDisplayFoodData(); // Load lại dữ liệu
                    }
                });
            }).start();
        }
    }

    /**
     * Hàm helper lấy món ăn đang được chọn trong bảng foodTableManage.
     */
    private Food getSelectedFood() {
        if (foodTableManage == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Bảng món ăn chưa được khởi tạo.");
            return null;
        }
        Food selectedFood = foodTableManage.getSelectionModel().getSelectedItem();
        if (selectedFood == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một món ăn trong bảng trước.");
        }
        return selectedFood;
    }

    /** Hàm showAlert tiện ích */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void setAdminContainerController(AdminContainerController adminContainerController) {
        this.adminContainerController = adminContainerController;
    }
}