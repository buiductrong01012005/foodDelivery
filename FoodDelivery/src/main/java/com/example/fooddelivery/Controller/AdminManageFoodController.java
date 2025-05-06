package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.FoodDAO;
import com.example.fooddelivery.Model.Food;
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
import java.util.stream.Collectors;

public class AdminManageFoodController implements Initializable {

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
    @FXML private Button detailFoodButton;

    private ObservableList<Food> allFoodsList = FXCollections.observableArrayList();
    private AdminContainerController adminContainerController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initializing AdminManageFoodController...");
        configureFoodTableColumns();
        loadAndDisplayFoodData();
        if (searchFoodField != null) {
            searchFoodField.textProperty().addListener((obs, oldVal, newVal) -> filterFoodData(newVal));
        }
    }

    // Setter cho AdminContainerController
    public void setAdminContainerController(AdminContainerController adminContainerController) {
        this.adminContainerController = adminContainerController;
    }


    private void configureFoodTableColumns() {
        if (foodTableManage == null) {
            System.err.println("ERROR: foodTableManage is null! Check FXML fx:id.");
            return;
        }
        System.out.println("INFO: Configuring columns for foodTableManage...");
        setupColumnFactory(foodIdColManage, "food_id");
        setupColumnFactory(foodNameColManage, "name");
        setupColumnFactory(foodCategoryColManage, "category_name");
        setupColumnFactory(foodPriceColManage, "price");
        setupColumnFactory(foodStatusColManage, "availability_status");
        foodTableManage.setPlaceholder(new Label("Đang tải dữ liệu món ăn..."));
    }

    private <S, T> void setupColumnFactory(TableColumn<S, T> column, String propertyName) {
        if (column != null) {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        } else {
            System.err.println("WARN: TableColumn for property '" + propertyName + "' is null. Check FXML fx:id.");
        }
    }

    void loadAndDisplayFoodData() {
        System.out.println("INFO: Initiating food data loading...");
        if (foodTableManage == null) {
            System.err.println("ERROR: foodTableManage is null. Cannot load data.");
            return;
        }
        foodTableManage.setPlaceholder(new Label("Đang tải dữ liệu..."));
        foodTableManage.getItems().clear();

        new Thread(() -> {
            ObservableList<Food> foods; // No need to initialize here
            try {
                foods = FoodDAO.getAllFoods();
                this.allFoodsList = foods != null ? foods : FXCollections.observableArrayList();
                System.out.println("INFO: DAO returned " + this.allFoodsList.size() + " foods.");

                Platform.runLater(() -> {
                    if (foodTableManage != null) {
                        foodTableManage.setItems(this.allFoodsList);
                        foodTableManage.setPlaceholder(new Label(this.allFoodsList.isEmpty() ? "Không có món ăn nào." : ""));
                        System.out.println("INFO: Food table updated with all loaded foods.");
                        filterFoodData(searchFoodField.getText());
                    }
                });

            } catch (Exception e) {
                System.err.println("ERROR: Unexpected error loading food data: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (foodTableManage != null) {
                        showAlert(Alert.AlertType.ERROR, "Lỗi Tải Dữ Liệu", "Lỗi: " + e.getMessage());
                        foodTableManage.setPlaceholder(new Label("Lỗi tải dữ liệu món ăn."));
                    }
                });
            }
        }).start();
    }

    @FXML
    private void onSearchFoodKeywordInput(ActionEvent event) {
        filterFoodData(searchFoodField.getText());
    }

    private void filterFoodData(String keyword) {
        if (allFoodsList == null) {
            System.out.println("WARN: allFoodsList is null, cannot filter.");
            return;
        }
        System.out.println("INFO: Filtering food with keyword: " + keyword);

        ObservableList<Food> filteredList;
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList = allFoodsList; //hien thi tat ca neu thanh tim kiem trong.
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
            System.err.println("ERROR: foodTableManage is null when trying to display search results.");
        }
    }

    @FXML
    private void onAddFoodButtonClick(ActionEvent event) {
        System.out.println("INFO: Add new food button clicked.");
        if (adminContainerController != null) {
            adminContainerController.requestFoodAdd();
        } else {
            handleMissingContainerError("Thêm Món");
        }
    }

    @FXML
    private void onEditFoodButtonClick(ActionEvent event) {
        Food selectedFood = getSelectedFood();
        if (selectedFood == null) return;

        System.out.println("INFO: Edit food button clicked for food ID: " + selectedFood.getFood_id());
        if (adminContainerController != null) {
            adminContainerController.requestFoodEdit(selectedFood);
        } else {
            handleMissingContainerError("Chỉnh Sửa Món");
        }
    }

    @FXML
    public void onDetailFoodButtonClick(ActionEvent actionEvent) {
        Food selectedFood = getSelectedFood();
        if (selectedFood == null) return;

        System.out.println("INFO: Detail food button clicked for food ID: " + selectedFood.getFood_id());
        if (adminContainerController != null) {
            adminContainerController.requestFoodView(selectedFood);
        } else {
            handleMissingContainerError("Xem Chi Tiết Món");
        }
    }


    @FXML
    private void onDeleteFoodButtonClick(ActionEvent event) {
        Food selectedFood = getSelectedFood();
        if (selectedFood == null) return;

        System.out.println("INFO: Delete food button clicked for food ID: " + selectedFood.getFood_id());

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận xóa");
        confirmation.setHeaderText("Xóa món ăn: " + selectedFood.getName());
        confirmation.setContentText("Bạn có chắc muốn xóa món ăn này không?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                boolean deleted = false;
                String message;
                try {
                    deleted = FoodDAO.deleteFood(selectedFood.getFood_id());
                    if (deleted) {
                        message = "Đã xóa món ăn '" + selectedFood.getName() + "' thành công.";
                    } else {
                        message = "Không thể xóa món ăn (có thể do không tìm thấy hoặc lỗi khác).";
                    }
                } catch (SQLException e) {
                    message = "Lỗi SQL khi xóa món ăn: " + e.getMessage();
                    System.err.println("ERROR: Lỗi SQL khi xóa food ID " + selectedFood.getFood_id()+ ": " + e.getMessage());
                    e.printStackTrace();
                }


                final boolean finalDeleted = deleted;
                final String finalMessage = message;
                Platform.runLater(() -> {
                    showAlert(finalDeleted ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            finalDeleted ? "Thành công" : "Thất bại",
                            finalMessage);
                    if (finalDeleted) {
                        loadAndDisplayFoodData(); // Reload data neu da xoa thanh cong.
                    }
                });
            }).start();
        }
    }

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

    private void handleMissingContainerError(String actionName) {
        System.err.println("ERROR: AdminContainerController is null. Cannot perform '" + actionName + "' action.");
        showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể thực hiện hành động '" + actionName + "'. Vui lòng liên hệ quản trị viên.");
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        // Ensure alerts are always shown on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(alertType);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(content);
                alert.showAndWait();
            });
        } else {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
}