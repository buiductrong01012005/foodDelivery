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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FoodInforController implements Initializable {

    public enum FormMode { VIEW, EDIT, ADD }

    private static final String DEFAULT_FOOD_IMAGE_PATH = "/images/default_food.png"; // *** Đường dẫn ảnh mặc định

    private FormMode currentMode;
    private Food currentFood;
    private int currentAdminId = 1; // Sẽ được AdminContainerController set
    private Consumer<Boolean> onCompleteCallback;
    private ObservableList<String> categoryNamesList = FXCollections.observableArrayList();
    private Image defaultFoodImage;

    @FXML private AnchorPane rootFoodInfoPane;
    @FXML private Label formTitleLabel;

    @FXML private ImageView foodImageView;
    @FXML private Label foodNameLabel, categoryNameLabel, priceLabel, statusLabel, descriptionLabel;

    @FXML private TextField foodNameField, priceField;
    @FXML private ComboBox<String> categoryComboBox, statusComboBox;
    @FXML private TextArea descriptionArea;

    @FXML private Button saveButton, addItemButton, cancelButton, closeButton;

    @FXML private TableView<ReviewDisplay> reviewsTableView;
    @FXML private TableColumn<ReviewDisplay, Integer> idColumn;
    @FXML private TableColumn<ReviewDisplay, String> userNameColumnReview;
    @FXML private TableColumn<ReviewDisplay, String> emailColumnReview;
    @FXML private TableColumn<ReviewDisplay, String> phoneNumberColumnReview;
    @FXML private TableColumn<ReviewDisplay, String> reviewColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDefaultFoodImage();
        if (foodImageView != null) {
            foodImageView.setImage(defaultFoodImage);
        }

        setupReviewTableColumns();
        loadCategoryNamesToComboBox();
        if (statusComboBox != null) {
            statusComboBox.setItems(FXCollections.observableArrayList("Available", "Unavailable"));
        }
        setupPriceFieldListener();
    }

    // *** Hàm tải ảnh mặc định ***
    private void loadDefaultFoodImage() {
        try {
            InputStream defaultImageStream = getClass().getResourceAsStream(DEFAULT_FOOD_IMAGE_PATH);
            if (defaultImageStream != null) {
                this.defaultFoodImage = new Image(defaultImageStream);
            } else {
                System.err.println("Không tìm thấy ảnh mặc định món ăn: " + DEFAULT_FOOD_IMAGE_PATH);
                this.defaultFoodImage = null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh mặc định món ăn: " + e.getMessage());
            this.defaultFoodImage = null;
        }
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
            priceField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    priceField.setText(newValue.replaceAll("[^\\d]", ""));
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
            if (foodImageView != null) {
                foodImageView.setImage(defaultFoodImage);
            }
            if (this.onCompleteCallback != null) this.onCompleteCallback.accept(false);
            return;
        }

        if (foodImageView != null) {
            foodImageView.setImage(defaultFoodImage);
        }

        populateDataToControls();
        setUIMode(mode == FormMode.EDIT || mode == FormMode.ADD);
        loadReviewsForFood( (mode != FormMode.ADD && this.currentFood != null) ? this.currentFood.getFood_id() : -1 );
    }

    private void populateDataToControls() {
        if (currentFood == null) return;

        if (foodImageView != null) {
            String imageFilename = currentFood.getImagePath();
            Image specificFoodImage = loadImageByName(imageFilename);
            foodImageView.setImage(specificFoodImage != null ? specificFoodImage : defaultFoodImage);
        }

        foodNameLabel.setText(getOrDefaultString(currentFood.getName()));
        categoryNameLabel.setText(getOrDefaultString(currentFood.getCategory_name()));
        priceLabel.setText(currentFood.getPrice() > 0 ? formatPrice(currentFood.getPrice()) : "[Chưa có]");
        statusLabel.setText(getOrDefaultString(currentFood.getAvailability_status()));
        descriptionLabel.setText(getOrDefaultString(currentFood.getDescription()));
        descriptionLabel.setWrapText(true);

        foodNameField.setText(currentFood.getName());
        if (categoryComboBox != null) categoryComboBox.setValue(currentFood.getCategory_name());
        priceField.setText(currentFood.getPrice() > 0 ? String.valueOf((int)currentFood.getPrice()) : "");
        if (statusComboBox != null) statusComboBox.setValue(currentFood.getAvailability_status());
        descriptionArea.setText(currentFood.getDescription());
        descriptionArea.setWrapText(true);
    }

    // *** Hàm helper để tải ảnh theo tên file từ resources/images ***
    private Image loadImageByName(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        String resourcePath = "../images/" + filename.trim();
        try {
            InputStream imageStream = getClass().getResourceAsStream(resourcePath);
            if (imageStream != null) {
                return new Image(imageStream);
            } else {
                System.err.println("Không tìm thấy resource ảnh: " + resourcePath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải resource ảnh: " + resourcePath + " - " + e.getMessage());
            return null;
        }
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

        if(currentMode == FormMode.ADD) {
            reviewsTableView.getItems().clear();
            reviewsTableView.setPlaceholder(new Label("Món mới chưa có đánh giá."));
        }

        setNodeVisibility(foodImageView, true);
    }

    private void toggleDisplayAndInput(Label displayLabel, Node inputField, boolean showDisplay) {
        setNodeVisibility(displayLabel, showDisplay);
        setNodeVisibility(inputField, !showDisplay);
        if (displayLabel != null) displayLabel.setWrapText(true);
        if (inputField instanceof TextArea) ((TextArea) inputField).setWrapText(true);

    }

    private void loadReviewsForFood(int foodId) {
        if (reviewsTableView == null) return;
        reviewsTableView.getItems().clear();
        if (foodId <= 0 || currentMode == FormMode.ADD) {
            reviewsTableView.setPlaceholder(new Label(currentMode == FormMode.ADD ? "Món mới chưa có đánh giá." : "Chọn món ăn hợp lệ để xem đánh giá."));
            return;
        }
        try {
            ObservableList<ReviewDisplay> reviews = FoodDAO.getReviewsForFood(foodId);
            reviewsTableView.setItems(reviews);
            reviewsTableView.setPlaceholder(new Label(reviews.isEmpty() ? "Món ăn này chưa có đánh giá nào." : ""));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Đánh Giá", "Không thể tải đánh giá: " + e.getMessage());
            reviewsTableView.setPlaceholder(new Label("Lỗi khi tải đánh giá."));
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
            if (categoryId <= 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Loại Món", "Không tìm thấy ID hợp lệ cho loại món: '" + selectedCategoryName + "'.");
                categoryComboBox.requestFocus(); return;
            }
            currentFood.setCategory_id(categoryId);
            currentFood.setCategory_name(selectedCategoryName);
        } catch (SQLException e) {
            handleSqlException(e, "lấy ID loại món"); return;
        }
        currentFood.setDescription(descriptionArea.getText().trim());
        currentFood.setPrice(Double.parseDouble(priceField.getText()));
        currentFood.setAvailability_status(statusComboBox.getValue());
        currentFood.setUpdated_by(currentAdminId);

        // *** Xử lý lưu ảnh (Nếu có chức năng upload) ***
        // TODO: Nếu có chức năng upload ảnh mới, bạn cần:
        // 1. Lấy đường dẫn file ảnh mới đã upload.
        // 2. Lưu tên file mới vào currentFood.setImagePath("ten_file_moi.jpg");
        // 3. Có thể cần copy file ảnh vào thư mục /images nếu chưa có.

        // Thực hiện lưu hoặc thêm vào DB
        if (currentMode == FormMode.EDIT) {
            try {
                if (FoodDAO.updateFood(currentFood)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Món ăn đã được cập nhật.");
                    finishAction(true); // Báo thành công và dữ liệu đã thay đổi
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật món ăn. Có thể không có thay đổi nào được thực hiện.");
                    // Không gọi finishAction(true) nếu không thành công
                }
            } catch (SQLException e) { handleSqlException(e, "cập nhật món ăn"); }
        } else if (currentMode == FormMode.ADD) {
            currentFood.setCreated_by(currentAdminId);
            try {
                Food addedFood = FoodDAO.addFood(currentFood);
                if (addedFood != null && addedFood.getFood_id() > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm món ăn mới: " + addedFood.getName());
                    finishAction(true);// Báo thành công và dữ liệu đã thay đổi
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm món ăn mới vào cơ sở dữ liệu.");
                }
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

        String priceText = priceField.getText().trim();
        if (priceText.isEmpty()) return validationFail("Giá không được trống.", priceField);
        try {
            double priceValue = Double.parseDouble(priceText);
            if (priceValue < 0) return validationFail("Giá không thể âm.", priceField);
        } catch (NumberFormatException e) {
            return validationFail("Giá phải là một số hợp lệ.", priceField);
        }

        return true;
    }

    // --- Các hàm helper ---
    private boolean validationFail(String message, Control fieldToFocus) {
        showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", message);
        if (fieldToFocus != null) fieldToFocus.requestFocus();
        return false; // Trả về false để dừng xử lý
    }
    private void setNodeVisibility(Node node, boolean isVisible) {
        if (node != null) {
            node.setVisible(isVisible);
            node.setManaged(isVisible);
        }
    }
    private String getOrDefaultString(String val) { return (val != null && !val.trim().isEmpty()) ? val : "[N/A]"; }
    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        return formatter.format((long)price); // Ép kiểu về long để bỏ phần thập phân
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void handleFormError(String message) {
        showAlert(Alert.AlertType.ERROR, "Lỗi Form", message);
    }
    private void handleSqlException(SQLException e, String action) {
        showAlert(Alert.AlertType.ERROR, "Lỗi Cơ Sở Dữ Liệu", "Đã xảy ra lỗi khi " + action + ": " + e.getMessage());
        e.printStackTrace();
    }
}