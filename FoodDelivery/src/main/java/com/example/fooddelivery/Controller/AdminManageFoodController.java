package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.FoodDAO; // <<< BẠN CẦN TẠO DAO NÀY
import com.example.fooddelivery.Model.Food;  // <<< Import Model Food đã có
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

// import java.math.BigDecimal; // Không dùng BigDecimal nữa, dùng double
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminManageFoodController implements Initializable {

    @FXML private AnchorPane manageFoodPaneRoot;
    @FXML private TextField searchFoodField;
    @FXML private Button addFoodButton;
    @FXML private TableView<Food> foodTableManage; // <<< Đã đổi thành TableView<Food>
    @FXML private TableColumn<Food, Integer> foodIdColManage; // <<< Đổi thành Integer (tương ứng int)
    @FXML private TableColumn<Food, String> foodNameColManage; // <<< Kiểu String
    @FXML private TableColumn<Food, Integer> foodCategoryColManage; // <<< Đổi thành Integer (cho category_id) - **Hoặc String nếu bạn muốn hiển thị tên loại**
    @FXML private TableColumn<Food, Double> foodPriceColManage; // <<< Đổi thành Double (tương ứng double)
    @FXML private TableColumn<Food, String> foodStatusColManage; // <<< Kiểu String
    @FXML private Button editFoodButton;
    @FXML private Button deleteFoodButton;

    private AdminContainerController adminContainerController;
    // Khởi tạo danh sách với kiểu Food cụ thể
    private ObservableList<Food> allFoodList = FXCollections.observableArrayList(); // <<< Đã đổi thành ObservableList<Food>
    private boolean initialDataLoaded = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminManageFoodController initialized.");
        configureFoodTableColumns();
    }

    public void setAdminContainerController(AdminContainerController controller) { this.adminContainerController = controller; }

    public void loadDataIfNeeded() {
        if (!initialDataLoaded) {
            System.out.println("AdminManageFoodController: Loading initial food data...");
            loadAndDisplayFoodData();
            initialDataLoaded = true;
        } else {
            System.out.println("AdminManageFoodController: Data already loaded. Applying filter.");
            filterFoodData(searchFoodField.getText());
        }
    }

    public void refreshFoodTable() {
        System.out.println("AdminManageFoodController: Refreshing food data...");
        loadAndDisplayFoodData();
    }

    private void configureFoodTableColumns() {
        System.out.println("AdminManageFoodController: Configuring table...");
        if (foodTableManage == null) { /*...*/ return;}

        // Sử dụng tên thuộc tính khớp với tên biến/getter trong Food.java
        setupColumnFactory(foodIdColManage, "food_id"); // Khớp với getFood_id() -> food_id
        setupColumnFactory(foodNameColManage, "name");    // Khớp với getName() -> name
        setupColumnFactory(foodCategoryColManage, "category_id"); // Khớp với getCategory_id() -> category_id
        // Nếu bạn muốn hiển thị tên Category thay vì ID, bạn cần sửa Model Food hoặc xử lý trong DAO/Controller
        setupColumnFactory(foodPriceColManage, "price");   // Khớp với getPrice() -> price
        setupColumnFactory(foodStatusColManage, "availability_status"); // Khớp với getAvailability_status() -> availability_status

        foodTableManage.setPlaceholder(new Label("Chưa tải dữ liệu món ăn..."));

        // Định dạng cột giá tiền (dùng kiểu Double)
        if (foodPriceColManage != null) {
            foodPriceColManage.setCellFactory(tc -> new TableCell<Food, Double>() {
                @Override
                protected void updateItem(Double price, boolean empty) {
                    super.updateItem(price, empty);
                    if (empty || price == null) {
                        setText(null);
                    } else {
                        // Định dạng tiền tệ Việt Nam cho double
                        setText(String.format("%,.0f đ", price));
                    }
                }
            });
        }

        // Định dạng cột Category ID (ví dụ hiển thị "Loại: 1", "Loại: 2")
        // Hoặc bạn có thể tạo CellFactory phức tạp hơn để tra cứu tên loại từ ID
        if (foodCategoryColManage != null) {
            foodCategoryColManage.setCellFactory(tc -> new TableCell<Food, Integer>() {
                @Override
                protected void updateItem(Integer categoryId, boolean empty) {
                    super.updateItem(categoryId, empty);
                    if (empty || categoryId == null || categoryId == 0) { // Giả sử 0 là không hợp lệ
                        setText(null);
                    } else {
                        // Tạm thời chỉ hiển thị ID
                        setText(String.valueOf(categoryId));
                        // TODO: Thay bằng tra cứu tên Category từ ID nếu cần
                        // setText(CategoryDAO.getCategoryNameById(categoryId));
                    }
                }
            });
        }
    }

    // Helper setup cột (giữ nguyên)
    private <S, T> void setupColumnFactory(TableColumn<S, T> column, String propertyName) {
        if (column != null && propertyName != null && !propertyName.isEmpty()) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        } else { /*...*/ }
    }

    private void loadAndDisplayFoodData() {
        if (foodTableManage == null) return;
        foodTableManage.setPlaceholder(new Label("Đang tải món ăn..."));

        // *** BẠN CẦN TẠO FoodDAO với phương thức getAllFoods() trả về ObservableList<Food> ***
        new Thread(() -> {
            try {
                // Gọi FoodDAO để lấy dữ liệu
                ObservableList<Food> foods = FoodDAO.getAllFoods(); // <<< GỌI DAO
                Platform.runLater(() -> {
                    // Cập nhật danh sách chính
                    this.allFoodList.setAll(foods != null ? foods : FXCollections.observableArrayList());
                    System.out.println("AdminManageFoodController: Food data fetched (" + allFoodList.size() + " items).");
                    // Áp dụng bộ lọc (nếu có)
                    filterFoodData(searchFoodField.getText());
                    // Set items cho TableView - Đã đúng kiểu Food
                    foodTableManage.setItems(this.allFoodList); // <<< SẼ HOẠT ĐỘNG
                    foodTableManage.setPlaceholder(new Label(allFoodList.isEmpty() ? "Không có món ăn nào." : ""));
                });
            } catch (Exception e) {
                System.err.println("AdminManageFoodController Error loading food: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (foodTableManage != null) foodTableManage.setPlaceholder(new Label("Lỗi tải dữ liệu món ăn."));
                });
            }
        }).start();

        // Tạm thời để tránh lỗi (xóa đi khi có FoodDAO)
        /*
         Platform.runLater(() -> {
             this.allFoodList.clear();
             foodTableManage.setItems(this.allFoodList); // Bây giờ set được list rỗng
             foodTableManage.setPlaceholder(new Label("Chức năng đang phát triển (Cần FoodDAO)."));
         });
         */
    }

    @FXML
    private void onSearchFoodKeywordInput(ActionEvent event) {
        filterFoodData(searchFoodField.getText());
    }

    private void filterFoodData(String keyword) {
        if (foodTableManage == null) return;
        if (allFoodList.isEmpty() && (keyword == null || keyword.isEmpty())) {
            foodTableManage.setItems(allFoodList);
            foodTableManage.setPlaceholder(new Label("Không có món ăn nào."));
            return;
        }

        ObservableList<Food> filteredList;
        String lowerCaseKeyword = (keyword == null) ? "" : keyword.toLowerCase().trim();
        if (lowerCaseKeyword.isEmpty()) {
            filteredList = allFoodList;
        } else {
            System.out.println("AdminManageFoodController: Filtering food with keyword: " + lowerCaseKeyword);
            filteredList = allFoodList.stream()
                    .filter(food -> foodMatchesKeyword(food, lowerCaseKeyword)) // Dùng hàm lọc
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
        foodTableManage.setItems(filteredList); // Cập nhật bảng
        foodTableManage.setPlaceholder(new Label(filteredList.isEmpty() ? "Không tìm thấy kết quả phù hợp." : ""));
    }

    // Hàm lọc dựa trên Model Food hiện tại
    private boolean foodMatchesKeyword(Food food, String lowerCaseKeyword) {
        if (food == null) return false;
        // Lọc theo tên (name) và mô tả (description) - ví dụ
        return (food.getName() != null && food.getName().toLowerCase().contains(lowerCaseKeyword)) ||
                (food.getDescription() != null && food.getDescription().toLowerCase().contains(lowerCaseKeyword)) ||
                (String.valueOf(food.getFood_id()).contains(lowerCaseKeyword)); // Thêm tìm theo ID
        // Thêm các trường khác nếu muốn: food.getCategory_id(), food.getAvailability_status()
    }


    @FXML
    private void onAddFoodButtonClick(ActionEvent event) {
        System.out.println("AdminManageFoodController: Add Food button clicked.");
        // TODO: Mở dialog/form thêm món ăn mới, sau đó gọi refreshFoodTable()
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng thêm món ăn đang được phát triển.");
    }

    @FXML
    private void onEditFoodButtonClick(ActionEvent event) {
        Food selectedFood = getSelectedFood(); // Lấy đối tượng Food
        if (selectedFood == null) return;
        System.out.println("AdminManageFoodController: Edit Food button clicked for ID: " + selectedFood.getFood_id());
        // TODO: Mở dialog/form sửa món ăn với thông tin của selectedFood, sau đó gọi refreshFoodTable()
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng sửa món ăn đang được phát triển.");
    }

    @FXML
    private void onDeleteFoodButtonClick(ActionEvent event) {
        Food selectedFood = getSelectedFood(); // Lấy đối tượng Food
        if (selectedFood == null) return;
        System.out.println("AdminManageFoodController: Delete Food button clicked for ID: " + selectedFood.getFood_id());

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa món '" + selectedFood.getName() + "' không?",
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Xác nhận xóa");
        confirmation.setHeaderText("Xóa món ăn");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // TODO: Gọi FoodDAO.deleteFood(selectedFood.getFood_id()) trong Thread
                deleteFoodAsync(selectedFood.getFood_id(), selectedFood.getName()); // Gọi hàm xóa async
            }
        });
    }

    // Hàm xóa món ăn bất đồng bộ (ví dụ)
    private void deleteFoodAsync(int foodId, String foodName) {
        new Thread(() -> {
            boolean deleted = false;
            String message;
            try {
                // deleted = FoodDAO.deleteFood(foodId); // <<< GỌI DAO
                System.out.println("Placeholder: Simulating delete for food ID " + foodId); // Xóa dòng này khi có DAO
                deleted = true; // Giả lập thành công để test
                message = deleted ? "Đã xóa món '" + foodName + "'." : "Xóa món ăn thất bại.";
            } catch (Exception e) { // Bắt Exception chung hoặc SQLException cụ thể
                message = "Lỗi khi xóa món ăn: " + e.getMessage();
                deleted = false;
                e.printStackTrace();
            }
            final boolean finalDeleted = deleted;
            final String finalMessage = message;
            Platform.runLater(() -> {
                showAlert(finalDeleted ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, finalDeleted ? "Thành công" : "Thất bại", finalMessage);
                if (finalDeleted) {
                    refreshFoodTable(); // Load lại bảng nếu xóa thành công
                }
            });
        }).start();
    }


    // Helper lấy món ăn được chọn
    private Food getSelectedFood() {
        if (foodTableManage == null || foodTableManage.getSelectionModel() == null) { /*...*/ return null; }
        Food selected = foodTableManage.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một món ăn trong bảng.");
        }
        return selected;
    }

    // Hàm showAlert (giữ nguyên)
    private void showAlert(Alert.AlertType alertType, String title, String content) { /*...*/ }
    private void showAlertInternal(Alert.AlertType alertType, String title, String content) { /*...*/ }
}