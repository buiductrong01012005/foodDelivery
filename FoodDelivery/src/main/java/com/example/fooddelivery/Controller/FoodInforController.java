package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.FoodDAO;
import com.example.fooddelivery.Model.Food;
import com.example.fooddelivery.Model.ReviewDisplay;
import com.example.fooddelivery.Utils.NameConvertUtil;
import com.example.fooddelivery.Service.ImageRenService;
import com.example.fooddelivery.Service.ImageSearchService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class FoodInforController implements Initializable {

    public enum FormMode { VIEW, EDIT, ADD }

    private static final String DEFAULT_FOOD_IMAGE_PATH = "/images/default_food.png";

    private FormMode currentMode;
    private Food currentFood;
    private int currentAdminId = 1;
    private Consumer<Boolean> onCompleteCallback;
    private ObservableList<String> categoryNamesList = FXCollections.observableArrayList();
    private Image defaultFoodImage;
    // Bỏ currentLoadedImageName vì logic giờ đây sẽ dựa vào currentFood.getImagePath()
    // hoặc tải mới khi ADD.

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
    @FXML private TableColumn<ReviewDisplay, String> statusColumnReview;

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

    private void loadDefaultFoodImage() {
        try {
            InputStream defaultImageStream = getClass().getResourceAsStream(DEFAULT_FOOD_IMAGE_PATH);
            if (defaultImageStream != null) {
                this.defaultFoodImage = new Image(defaultImageStream);
            } else {
                System.err.println("Không tìm thấy ảnh mặc định món ăn: " + DEFAULT_FOOD_IMAGE_PATH);
                // Không gán null ở đây nữa, để defaultFoodImage có thể vẫn giữ giá trị cũ nếu có
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh mặc định món ăn: " + e.getMessage());
        }
        // Nếu defaultFoodImage vẫn là null, loadImageByName sẽ xử lý
    }

    private void setupReviewTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
        userNameColumnReview.setCellValueFactory(new PropertyValueFactory<>("userName"));
        emailColumnReview.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        phoneNumberColumnReview.setCellValueFactory(new PropertyValueFactory<>("userPhoneNumber"));
        reviewColumn.setCellValueFactory(new PropertyValueFactory<>("reviewComment"));
        reviewsTableView.setPlaceholder(new Label("Chưa có đánh giá cho món ăn này."));

        if (statusColumnReview != null) {
            statusColumnReview.setCellValueFactory(new PropertyValueFactory<>("status"));
            ObservableList<String> reviewStatusOptions = FXCollections.observableArrayList("Show", "Hide");
            statusColumnReview.setCellFactory(ComboBoxTableCell.forTableColumn(reviewStatusOptions));
            statusColumnReview.setOnEditCommit(event -> {
                ReviewDisplay selectedReview = event.getRowValue();
                String newStatus = event.getNewValue();
                String oldStatus = selectedReview.getStatus();
                if (selectedReview != null && newStatus != null && !oldStatus.equals(newStatus)) {
                    try {
                        selectedReview.setStatus(newStatus);
                        boolean success = FoodDAO.updateReviewStatus(selectedReview.getReviewId(), newStatus);
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật trạng thái bình luận ID: " + selectedReview.getReviewId());
                        } else {
                            selectedReview.setStatus(oldStatus);
                            reviewsTableView.refresh();
                            showAlert(Alert.AlertType.ERROR, "Lỗi cập nhật", "Không thể cập nhật trạng thái bình luận vào DB.");
                        }
                    } catch (SQLException e) {
                        selectedReview.setStatus(oldStatus);
                        reviewsTableView.refresh();
                        handleSqlException(e, "cập nhật trạng thái bình luận");
                    }
                }
            });
        }
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
        this.currentMode = mode;
        this.onCompleteCallback = callback;
        if (mode == FormMode.ADD) {
            this.currentFood = new Food(); // Tạo đối tượng Food mới cho chế độ ADD
            // Đảm bảo imagePath là null cho món mới
            this.currentFood.setImagePath(null);
        } else {
            this.currentFood = food;
        }

        if (this.currentFood == null && (mode == FormMode.VIEW || mode == FormMode.EDIT)) {
            handleFormError("Không có dữ liệu món ăn.");
            if (foodImageView != null) foodImageView.setImage(defaultFoodImage);
            if (this.onCompleteCallback != null) this.onCompleteCallback.accept(false);
            return;
        }

        if (foodImageView != null) foodImageView.setImage(defaultFoodImage);

        populateDataToControls();
        setUIMode(mode == FormMode.EDIT || mode == FormMode.ADD);
        loadReviewsForFood((mode != FormMode.ADD && this.currentFood != null && this.currentFood.getFood_id() > 0) ? this.currentFood.getFood_id() : -1);
    }

    private void populateDataToControls() {
        if (currentFood == null) {
            if (foodImageView != null) foodImageView.setImage(defaultFoodImage);
            foodNameLabel.setText("[N/A]"); categoryNameLabel.setText("[N/A]"); priceLabel.setText("[N/A]");
            statusLabel.setText("[N/A]"); descriptionLabel.setText("[N/A]");
            foodNameField.clear(); categoryComboBox.setValue(null); priceField.clear();
            statusComboBox.setValue(null); descriptionArea.clear();
            return;
        }

        if (foodImageView != null) {
            // currentFood.getImagePath() trả về TÊN FILE ĐƠN GIẢN (ví dụ: "com-suon.jpg")
            String imageFilenameFromDB = currentFood.getImagePath();
            Image specificFoodImage = loadImageByName(imageFilenameFromDB); // loadImageByName tự thêm "/images/"
            foodImageView.setImage(specificFoodImage); // specificFoodImage sẽ là defaultFoodImage nếu có lỗi
        }

        foodNameLabel.setText(getOrDefaultString(currentFood.getName()));
        categoryNameLabel.setText(getOrDefaultString(currentFood.getCategory_name()));
        priceLabel.setText(currentFood.getPrice() > 0 ? formatPrice(currentFood.getPrice()) : "[Chưa có]");
        statusLabel.setText(getOrDefaultString(currentFood.getAvailability_status()));
        descriptionLabel.setText(getOrDefaultString(currentFood.getDescription()));
        descriptionLabel.setWrapText(true);

        foodNameField.setText(currentFood.getName());
        if (categoryComboBox != null) categoryComboBox.setValue(currentFood.getCategory_name());
        priceField.setText(currentFood.getPrice() > 0 ? String.valueOf((int) currentFood.getPrice()) : "");
        if (statusComboBox != null) statusComboBox.setValue(currentFood.getAvailability_status());
        descriptionArea.setText(currentFood.getDescription());
        descriptionArea.setWrapText(true);
    }

    private Image loadImageByName(String simpleImageFileName) {
        if (this.defaultFoodImage == null) {
            loadDefaultFoodImage();
            if (this.defaultFoodImage == null) {
                System.err.println("loadImageByName: Ảnh mặc định không thể tải, trả về null.");
                return null;
            }
        }
        if (simpleImageFileName == null || simpleImageFileName.trim().isEmpty()) {
            return defaultFoodImage;
        }
        String resourcePath = "/" + simpleImageFileName.trim();
        try {
            InputStream imageStream = getClass().getResourceAsStream(resourcePath);
            if (imageStream != null) {
                return new Image(imageStream);
            } else {
                System.err.println("Không tìm thấy resource ảnh: " + resourcePath);
                return defaultFoodImage;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải resource ảnh: " + resourcePath + " - " + e.getMessage());
            return defaultFoodImage;
        }
    }

    private void setUIMode(boolean editingFood) {
        toggleDisplayAndInput(foodNameLabel, foodNameField, !editingFood);
        toggleDisplayAndInput(categoryNameLabel, categoryComboBox, !editingFood);
        toggleDisplayAndInput(priceLabel, priceField, !editingFood);
        toggleDisplayAndInput(statusLabel, statusComboBox, !editingFood);
        toggleDisplayAndInput(descriptionLabel, descriptionArea, !editingFood);

        setNodeVisibility(saveButton, editingFood && currentMode == FormMode.EDIT);
        setNodeVisibility(addItemButton, editingFood && currentMode == FormMode.ADD);
        setNodeVisibility(cancelButton, editingFood);
        setNodeVisibility(closeButton, !editingFood);

        if (currentMode == FormMode.VIEW) {
            formTitleLabel.setText("Chi Tiết Món Ăn");
        } else if (currentMode == FormMode.EDIT) {
            formTitleLabel.setText("Chỉnh Sửa Món Ăn");
        } else { // ADD Mode
            formTitleLabel.setText("Thêm Món Ăn Mới");
            if (foodImageView != null) foodImageView.setImage(defaultFoodImage);
            foodNameField.clear();
            if(categoryComboBox != null) categoryComboBox.getSelectionModel().clearSelection();
            priceField.clear();
            if(statusComboBox != null) statusComboBox.getSelectionModel().clearSelection();
            descriptionArea.clear();
            if (currentFood != null) currentFood.setImagePath(null); // Đảm bảo món mới không có ảnh ban đầu
        }

        if (reviewsTableView != null) {
            boolean canEditReviews = (currentMode == FormMode.EDIT);
            reviewsTableView.setEditable(canEditReviews);
            if (statusColumnReview != null) {
                statusColumnReview.setEditable(canEditReviews);
            }
            if(currentMode == FormMode.ADD) {
                reviewsTableView.getItems().clear();
                reviewsTableView.setPlaceholder(new Label("Món mới chưa có đánh giá."));
            }
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
        if (foodId <= 0) { // Đã bao gồm currentMode == FormMode.ADD vì foodId sẽ <=0
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

        if (currentMode == FormMode.ADD) {
            currentFood.setCreated_by(currentAdminId);
            // Luôn cố gắng tìm ảnh tự động cho món mới
            String foodNameForSearch = currentFood.getName();
            // Optional: final ProgressIndicator progressIndicator = new ProgressIndicator(); ...

            Task<String> autoFetchTask = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    System.out.println("Đang tìm ảnh cho: " + foodNameForSearch);
                    List<String> imageUrls = ImageSearchService.searchImageUrls(foodNameForSearch, 1);
                    if (!imageUrls.isEmpty()) {
                        String imageUrl = imageUrls.get(0);
                        String baseFileName = NameConvertUtil.toSlug(foodNameForSearch);
                        return ImageRenService.downloadAndSaveImage(imageUrl, baseFileName);
                    }
                    return null; // Không tìm thấy URL
                }
            };

            autoFetchTask.setOnSucceeded(workerStateEvent -> {
                // Optional: remove progressIndicator
                String downloadedSimpleFileName = autoFetchTask.getValue();
                if (downloadedSimpleFileName != null) {
                    currentFood.setImagePath("images/" + downloadedSimpleFileName); // Lưu TÊN FILE đơn giản
                    Image newImage = loadImageByName(downloadedSimpleFileName);
                    if (foodImageView != null) foodImageView.setImage(newImage);
                    System.out.println("Đã tự động tải và đặt ảnh: " + downloadedSimpleFileName);
                } else {
                    System.out.println("Không tìm thấy hoặc lỗi tải ảnh tự động. Món ăn sẽ không có ảnh.");
                    currentFood.setImagePath(null); // Đảm bảo imagePath là null nếu không có ảnh
                    if (foodImageView != null) foodImageView.setImage(defaultFoodImage);
                }
                saveFoodDataToDB(); // Gọi lưu DB sau khi Task hoàn tất
            });

            autoFetchTask.setOnFailed(workerStateEvent -> {
                // Optional: remove progressIndicator
                System.err.println("Lỗi khi tự động tải ảnh: " + autoFetchTask.getException().getMessage());
                currentFood.setImagePath(null); // Lỗi thì không có ảnh
                if (foodImageView != null) foodImageView.setImage(defaultFoodImage);
                saveFoodDataToDB(); // Vẫn lưu thông tin món ăn dù ảnh lỗi
            });

            new Thread(autoFetchTask).start();
            // KHÔNG GỌI saveFoodDataToDB() TRỰC TIẾP Ở ĐÂY

        } else if (currentMode == FormMode.EDIT) {
            // Trong chế độ EDIT, không tự động thay đổi ảnh.
            // imagePath của currentFood đã được load từ DB và sẽ được gửi đi khi update.
            saveFoodDataToDB();
        }
    }

    private void saveFoodDataToDB() {
        try {
            if (currentMode == FormMode.ADD) {
                Food addedFood = FoodDAO.addFood(currentFood);
                if (addedFood != null && addedFood.getFood_id() > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm món ăn mới: " + addedFood.getName());
                    finishAction(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể thêm món ăn mới vào cơ sở dữ liệu.");
                    finishAction(false);
                }
            } else if (currentMode == FormMode.EDIT) {
                if (FoodDAO.updateFood(currentFood)) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Món ăn đã được cập nhật.");
                    finishAction(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật món ăn.");
                    finishAction(false);
                }
            }
        } catch (SQLException e) {
            handleSqlException(e, (currentMode == FormMode.ADD ? "thêm món ăn" : "cập nhật món ăn"));
            finishAction(false);
        }
    }

    @FXML
    void handleCancelOrClose(ActionEvent event) {
        // Không còn currentLoadedImageName để reset
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

    private boolean validationFail(String message, Control fieldToFocus) {
        showAlert(Alert.AlertType.WARNING, "Dữ liệu không hợp lệ", message);
        if (fieldToFocus != null) fieldToFocus.requestFocus();
        return false;
    }
    private void setNodeVisibility(Node node, boolean isVisible) {
        if (node != null) { node.setVisible(isVisible); node.setManaged(isVisible); }
    }
    private String getOrDefaultString(String val) { return (val != null && !val.trim().isEmpty()) ? val : "[N/A]"; }
    private String formatPrice(double price) {
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        return formatter.format((long)price);
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title);
        alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
    private void handleFormError(String message) {
        showAlert(Alert.AlertType.ERROR, "Lỗi Form", message);
    }
    private void handleSqlException(SQLException e, String action) {
        showAlert(Alert.AlertType.ERROR, "Lỗi Cơ Sở Dữ Liệu", "Đã xảy ra lỗi khi " + action + ": " + e.getMessage());
        e.printStackTrace();
    }
}