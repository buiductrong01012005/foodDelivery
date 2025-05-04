package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.Food;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

public class FoodDetailController {

    @FXML private ImageView imgFood;
    @FXML private Text txtName, txtStore, txtAddress, txtStatus, txtPrice;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtQuantity;
    @FXML private Button btnAddCart;
    @FXML private Button btnPlus;
    @FXML private Button btnMinus;

    private Food food;
    private final int userId = 1; // giả định user đang đăng nhập

    public void setFood(Food food) {
        this.food = food;

        // Kiểm tra null cho các thành phần UI
        Objects.requireNonNull(txtName, "txtName is null");
        Objects.requireNonNull(txtStore, "txtStore is null");
        Objects.requireNonNull(txtAddress, "txtAddress is null");
        Objects.requireNonNull(txtStatus, "txtStatus is null");
        Objects.requireNonNull(txtPrice, "txtPrice is null");
        Objects.requireNonNull(txtDescription, "txtDescription is null");
        Objects.requireNonNull(txtQuantity, "txtQuantity is null");
        Objects.requireNonNull(btnAddCart, "btnAddCart is null");
        Objects.requireNonNull(btnPlus, "btnPlus is null");
        Objects.requireNonNull(btnMinus, "btnMinus is null");

        // Hiển thị dữ liệu
        txtName.setText(food.getName());
        txtStore.setText("ShopeeFood Store");
        txtAddress.setText("Hà Nội");
        txtStatus.setText(food.getAvailability_status());
        txtPrice.setText(String.format("%.0f VNĐ", food.getPrice()));
        txtDescription.setText(food.getDescription());
        txtQuantity.setText("1");

        try {
            imgFood.setImage(new Image(food.getImage_url(), true));
        } catch (Exception e) {
            System.out.println("Không thể load ảnh: " + e.getMessage());
        }

        // Tăng số lượng
        btnPlus.setOnAction(e -> {
            int current = parseQuantity();
            txtQuantity.setText(String.valueOf(current + 1));
        });

        // Giảm số lượng
        btnMinus.setOnAction(e -> {
            int current = parseQuantity();
            if (current > 1) {
                txtQuantity.setText(String.valueOf(current - 1));
            }
        });

        // Thêm vào giỏ hàng
        btnAddCart.setOnAction(e -> {
            int quantity = parseQuantity();
            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Số lượng không hợp lệ");
                return;
            }

            try (Connection conn = DatabaseConnector.connectDB()) {
                if (conn != null) {
                    String sql = "INSERT INTO cart_items (user_id, food_id, quantity, created_at) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, userId);
                    stmt.setInt(2, food.getFood_id());
                    stmt.setInt(3, quantity);
                    stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    stmt.executeUpdate();

                    showAlert(Alert.AlertType.INFORMATION, "Đã thêm vào giỏ hàng thành công!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Không thể thêm vào giỏ hàng.");
            }
        });
    }

    private int parseQuantity() {
        try {
            return Integer.parseInt(txtQuantity.getText().trim());
        } catch (Exception e) {
            return 1;
        }
    }

    private void showAlert(Alert.AlertType type, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
