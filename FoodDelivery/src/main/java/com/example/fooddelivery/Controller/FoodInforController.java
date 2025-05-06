package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.FoodDAO;
import com.example.fooddelivery.Model.Food;
import com.example.fooddelivery.Model.ReviewDisplay;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FoodInforController implements Initializable {

    public enum FormMode { VIEW, EDIT, ADD }

    private FormMode currentMode;
    private Food currentFood;
    private int currentAdminId = 1; // Sẽ được AdminContainerController set
    private Consumer<Boolean> onCompleteCallback;
    private ObservableList<String> categoryNamesList = FXCollections.observableArrayList();

    @FXML private AnchorPane rootFoodInfoPane; // ID của AnchorPane gốc trong FXML
    @FXML private Label formTitleLabel;

    // Labels để hiển thị (VIEW mode)
    @FXML private Label foodNameLabel, categoryNameLabel, priceLabel, statusLabel, descriptionLabel;

    // Input fields (EDIT/ADD mode)
    @FXML private TextField foodNameField, priceField;
    @FXML private ComboBox<String> categoryComboBox, statusComboBox;
    @FXML private TextArea descriptionArea;

    // Nút
    @FXML private Button saveButton, addItemButton, cancelButton, closeButton;

    // Bảng Review
    @FXML private TableView<ReviewDisplay> reviewsTableView;
    @FXML private TableColumn<ReviewDisplay, Integer> idColumn;
    @FXML private TableColumn<ReviewDisplay, String> userNameColumnReview;
    @FXML private TableColumn<ReviewDisplay, String> emailColumnReview;
    @FXML private TableColumn<ReviewDisplay, String> phoneNumberColumnReview;
    @FXML private TableColumn<ReviewDisplay, String> reviewColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupReviewTableColumns();
        loadCategoryNamesToComboBox();
        if (statusComboBox != null) { // Kiểm tra null trước khi dùng
            statusComboBox.setItems(FXCollections.observableArrayList("Available", "Unavailable"));
        }
        setupPriceFieldListener();
    }

    private void setupReviewTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
        userNameColumnReview.setCellValueFactory(new PropertyValueFactory<>("userName"));
        emailColumnReview.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        phoneNumberColumnReview.setCellValueFactory(new PropertyValueFactory<>("userPhoneNumber"));
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("reviewComment"));
        reviewsTableView.setPlaceholder(new Label("Chưa có đánh giá cho món ăn này."));
    }

    private void loadCategoryNamesToComboBox() {
        try {
            categoryNamesList = FoodDAO.getAllCategoryNames();
            if (categoryComboBox != null) categoryComboBox.setItems(categoryNamesList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải loại món", "Không thể tải danh sách loại món: " + e.getMessage());
            if (categoryComboBox != null) categoryComboBox.setItems(FXCollections.observableArrayList("N/A"));
        }
    }

    private void setupPriceFieldListener() {
        if (priceField != null) {
            priceField.textProperty().addListener((obs, ov, nv) -> {
                if (!nv.matches("\\d*\\.?\\d*")) { // Cho phép số thực
                    priceField.setText(nv.replaceAll("[^\\d.]", ""));
                }
            });
        }
    }

    public void setCurrentAdminId(int adminId) { this.currentAdminId = adminId; }

    public void loadFoodInformation(Food food, FormMode mode, Consumer<Boolean> callback) {
        this.currentFood = (mode == FormMode.ADD) ? new Food() : food;
        this.currentMode = mode;
        this.onCompleteCallback = callback;

        if (this.currentFood == null && (mode == FormMode.VIEW || mode == FormMode.EDIT)) {
            handleFormError("Không có dữ liệu món ăn để hiển thị/chỉnh sửa.");
            if (this.onCompleteCallback != null) this.onCompleteCallback.accept(false); // Báo lỗi và thoát
            return;
        }

        populateDataToControls();
        setUIMode(mode == FormMode.EDIT || mode == FormMode.ADD);
        loadReviewsForFood( (mode != FormMode.ADD && this.currentFood != null) ? this.currentFood.getFood_id() : -1 );
    }

    private void populateDataToControls() {
        if (currentFood == null) return;

        // Điền vào Labels
        foodNameLabel.setText(getOrDefaultString(currentFood.getName()));
        categoryNameLabel.setText(getOrDefaultString(currentFood.getCategory_name()));
        priceLabel.setText(currentFood.getPrice() > 0 ? formatPrice(currentFood.getPrice()) : "[Chưa có]");
        statusLabel.setText(getOrDefaultString(currentFood.getAvailability_status()));
        descriptionLabel.setText(getOrDefaultString(currentFood.getDescription()));

        // Điền vào Input Fields
        foodNameField.setText(currentFood.getName());
        if (categoryComboBox != null) categoryComboBox.setValue(currentFood.getCategory_name());
        priceField.setText(currentFood.getPrice() > 0 ? String.valueOf((int)currentFood.getPrice()) : "");
        if (statusComboBox != null) statusComboBox.setValue(currentFood.getAvailability_status());
        descriptionArea.setText(currentFood.getDescription());
    }

    private void setUIMode(boolean editing) {
        toggleDisplayAndInput(foodNameLabel, foodNameField, !editing);
        toggleDisplayAndInput(categoryNameLabel, categoryComboBox, !editing);
        toggleDisplayAndInput(priceLabel, priceField, !editing);
        toggleDisplayAndInput(statusLabel, statusComboBox, !editing);
        toggleDisplayAndInput(descriptionLabel, descriptionArea, !editing);

        setNodeVisibility(saveButton, editing && currentMode == FormMode.EDIT);
        setNodeVisibility(addItemButton, editing && currentMode == FormMode.ADD);
        setNodeVisibility(cancelButton, editing);
        setNodeVisibility(closeButton, !editing);

        if (currentMode == FormMode.VIEW) formTitleLabel.setText("Chi Tiết Món Ăn");
        else if (currentMode == FormMode.EDIT) formTitleLabel.setText("Chỉnh Sửa Món Ăn");
        else formTitleLabel.setText("Thêm Món Ăn Mới");
    }

    private void toggleDisplayAndInput(Label displayLabel, Node inputField, boolean showDisplay) {
        setNodeVisibility(displayLabel, showDisplay);
        setNodeVisibility(inputField, !showDisplay);
    }

    private void loadReviewsForFood(int foodId) {
        if (reviewsTableView == null) return;
        reviewsTableView.getItems().clear();
        if (foodId <= 0) {
            reviewsTableView.setPlaceholder(new Label(currentMode == FormMode.ADD ? "Món mới chưa có đánh giá." : "Chọn món ăn để xem đánh giá."));
            return;
        }
        try {
            ObservableList<ReviewDisplay> reviews = FoodDAO.getReviewsForFood(foodId);
            reviewsTableView.setItems(reviews);
            reviewsTableView.setPlaceholder(new Label(reviews.isEmpty() ? "Chưa có đánh giá nào." : ""));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Đánh Giá", "Không thể tải đánh giá: " + e.getMessage());
        }
    }

    @FXML
    void handleSaveOrAddItem(ActionEvent event) {
        if (!validateInputs()) return;

        currentFood.setName(foodNameField.getText().trim());
        try {
            String selectedCategoryName = categoryComboBox.getValue();
            if (selectedCategoryName == null || selectedCategoryName.isEmpty() || "N/A".equals(selectedCategoryName)) {
                validationFail("Vui lòng chọn loại món ăn hợp lệ.", categoryComboBox); return;
            }
            int categoryId = FoodDAO.getCategoryIdByName(selectedCategoryName);
            if (categoryId == -1) { // Không tìm thấy category ID
                showAlert(Alert.AlertType.ERROR, "Lỗi Loại Món", "Loại món '" + selectedCategoryName + "' không tồn tại hoặc không hợp lệ.");
                categoryComboBox.requestFocus(); return;
            }
            currentFood.setCategory_id(categoryId);
        } catch (SQLException e) {
            handleSqlException(e, "lấy ID loại món"); return;
        }
        currentFood.setDescription(descriptionArea.getText().trim());
        currentFood.setPrice(Double.parseDouble(priceField.getText()));
        currentFood.setAvailability_status(statusComboBox.getValue());
        currentFood.setUpdated_by(currentAdminId);

        if (currentMode == FormMode.EDIT) {
            try {
                if (FoodDAO.updateFood(currentFood)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Món ăn đã được cập nhật.");
                    finishAction(true);
                } else showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật món ăn. Dữ liệu có thể không thay đổi.");
            } catch (SQLException e) { handleSqlException(e, "cập nhật món ăn"); }
        } else if (currentMode == FormMode.ADD) {
            currentFood.setCreated_by(currentAdminId);
            try {
                Food addedFood = FoodDAO.addFood(currentFood); // addFood giờ trả về Food object
                if (addedFood != null && addedFood.getFood_id() > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm món ăn: " + addedFood.getName());
                    finishAction(true);
                } else showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm món ăn mới.");
            } catch (SQLException e) { handleSqlException(e, "thêm món ăn"); }
        }
    }

    @FXML
    void handleCancelOrClose(ActionEvent event) {
        finishAction(false);
    }

    private void finishAction(boolean dataChanged) {
        if (onCompleteCallback != null) {
            onCompleteCallback.accept(dataChanged);
        }
    }

    private boolean validateInputs() {
        if (foodNameField.getText().trim().isEmpty()) return validationFail("Tên món không được trống.", foodNameField);
        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().isEmpty() || "N/A".equals(categoryComboBox.getValue())) {
            return validationFail("Vui lòng chọn loại món.", categoryComboBox);
        }
        if (statusComboBox.getValue() == null || statusComboBox.getValue().isEmpty()) return validationFail("Vui lòng chọn tình trạng.", statusComboBox);
        if (priceField.getText().trim().isEmpty()) return validationFail("Giá không được trống.", priceField);
        try {
            if (Double.parseDouble(priceField.getText()) < 0) return validationFail("Giá không thể âm.", priceField);
        } catch (NumberFormatException e) { return validationFail("Giá phải là số.", priceField); }
        return true;
    }

    // --- Các hàm helper ---
    private boolean validationFail(String message, Control fieldToFocus) {
        showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", message);
        if (fieldToFocus != null) fieldToFocus.requestFocus();
        return false;
    }
    private void setNodeVisibility(Node node, boolean isVisible) {
        if (node != null) { node.setVisible(isVisible); node.setManaged(isVisible); }
    }
    private String getOrDefaultString(String val) { return (val != null && !val.trim().isEmpty()) ? val : "[N/A]"; }
    private String formatPrice(double price) { return new DecimalFormat("#,### VNĐ").format(price); }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title);
        alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
    private void handleFormError(String message) {
        showAlert(Alert.AlertType.ERROR, "Lỗi Form", message);
        if (onCompleteCallback != null) onCompleteCallback.accept(false);
    }
    private void handleSqlException(SQLException e, String action) {
        showAlert(Alert.AlertType.ERROR, "Lỗi DB", "Lỗi khi " + action + ": " + e.getMessage());
        e.printStackTrace();
    }
}