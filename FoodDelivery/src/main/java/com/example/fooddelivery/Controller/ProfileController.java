// ProfileController.java
package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.UserDAO;
import com.example.fooddelivery.Model.User;
import com.example.fooddelivery.Utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

public class ProfileController {

    @FXML private Label userIdLabel;
    @FXML private Label userNameLabel;

    @FXML private TextField fullNameField;
    @FXML private TextField ageField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;

    @FXML private ImageView userImage;

    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button addPhotoButton;

    private User currentUser;
    private String originalImageUrl;

    @FXML
    public void initialize() {
        currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            System.err.println("ERROR: No user in session.");
            return;
        }

        originalImageUrl = currentUser.getProfile_picture_url();

        userIdLabel.setText(String.valueOf(currentUser.getUser_id()));
        userNameLabel.setText(currentUser.getFull_name());
        fullNameField.setText(currentUser.getFull_name());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone_number());
        addressField.setText(currentUser.getAddress());

        if (currentUser.getDate_of_birth() != null) {
            int age = Period.between(currentUser.getDate_of_birth(), LocalDate.now()).getYears();
            ageField.setText(String.valueOf(age));
        }

        if (originalImageUrl != null && !originalImageUrl.isEmpty()) {
            userImage.setImage(new Image(originalImageUrl));
        }

        setEditable(false);
    }

    @FXML
    private void handleEdit() {
        setEditable(true);
        editButton.setVisible(false);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
        addPhotoButton.setVisible(true);
    }

    @FXML
    private void handleCancel() {
        fullNameField.setText(currentUser.getFull_name());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone_number());
        addressField.setText(currentUser.getAddress());

        if (currentUser.getDate_of_birth() != null) {
            int age = Period.between(currentUser.getDate_of_birth(), LocalDate.now()).getYears();
            ageField.setText(String.valueOf(age));
        }

        userImage.setImage(new Image(originalImageUrl));

        setEditable(false);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        addPhotoButton.setVisible(false);
        editButton.setVisible(true);
    }

    @FXML
    private void handleSave() {
        currentUser.setAddress(addressField.getText());
        currentUser.setPhone_number(phoneField.getText());

        try {
            int age = Integer.parseInt(ageField.getText().trim());
            if (age < 0 || age > 120) {
                showAlert("Tuổi không hợp lệ", "Vui lòng nhập tuổi hợp lệ (0-120)", Alert.AlertType.WARNING);
                return;
            }
            currentUser.setDate_of_birth(LocalDate.now().minusYears(age));
        } catch (NumberFormatException e) {
            showAlert("Tuổi không hợp lệ", "Tuổi phải là số nguyên", Alert.AlertType.WARNING);
            return;
        }

        currentUser.setProfile_picture_url(originalImageUrl);

        try {
            boolean updated = UserDAO.updateUserGeneralInfo(currentUser);
            if (updated) {
                showAlert("Thành công", "Thông tin cá nhân đã được cập nhật.", Alert.AlertType.INFORMATION);
                Session.setCurrentUser(currentUser);
                userNameLabel.setText(currentUser.getFull_name());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể cập nhật thông tin: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        setEditable(false);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        addPhotoButton.setVisible(false);
        editButton.setVisible(true);
    }

    @FXML
    private void handleAddPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh đại diện");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Hình ảnh", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(userImage.getScene().getWindow());
        if (selectedFile != null) {
            String imagePath = selectedFile.toURI().toString();
            userImage.setImage(new Image(imagePath));
            originalImageUrl = imagePath;
        }
    }

    private void setEditable(boolean editable) {
        addressField.setEditable(editable);
        ageField.setEditable(editable);
        phoneField.setEditable(editable);

        ageField.setOpacity(1.0);
        phoneField.setOpacity(1.0);

        fullNameField.setEditable(false);
        emailField.setEditable(false);

        fullNameField.setOpacity(0.6);
        emailField.setOpacity(0.6);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
