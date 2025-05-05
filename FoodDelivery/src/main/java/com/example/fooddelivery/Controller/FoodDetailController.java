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
import java.sql.ResultSet;
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
    private final int userId = 1; // Giả định user đăng nhập

    public void setFood(Food food) {
        this.food = food;

        Objects.requireNonNull(txtName);
        Objects.requireNonNull(txtStore);
        Objects.requireNonNull(txtAddress);
        Objects.requireNonNull(txtStatus);
        Objects.requireNonNull(txtPrice);
        Objects.requireNonNull(txtDescription);
        Objects.requireNonNull(txtQuantity);
        Objects.requireNonNull(btnAddCart);
        Objects.requireNonNull(btnPlus);
        Objects.requireNonNull(btnMinus);

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

        btnPlus.setOnAction(e -> {
            int current = parseQuantity();
            txtQuantity.setText(String.valueOf(current + 1));
        });

        btnMinus.setOnAction(e -> {
            int current = parseQuantity();
            if (current > 1) {
                txtQuantity.setText(String.valueOf(current - 1));
            }
        });

        btnAddCart.setOnAction(e -> {
            int quantity = parseQuantity();
            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Số lượng không hợp lệ");
                return;
            }

            try (Connection conn = DatabaseConnector.connectDB()) {
                if (conn != null) {
                    // Lấy cart_id theo userId
                    String getCartSQL = "SELECT cart_id FROM carts WHERE user_id = ?";
                    PreparedStatement cartStmt = conn.prepareStatement(getCartSQL);
                    cartStmt.setInt(1, userId);
                    ResultSet rs = cartStmt.executeQuery();

                    if (rs.next()) {
                        int cartId = rs.getInt("cart_id");
                        String sql = "INSERT INTO cart_items (cart_id, food_id, quantity, added_at) VALUES (?, ?, ?, ?)";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setInt(1, cartId);
                        stmt.setInt(2, food.getFood_id());
                        stmt.setInt(3, quantity);
                        stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                        stmt.executeUpdate();

                        showAlert(Alert.AlertType.INFORMATION, "Đã thêm vào giỏ hàng thành công!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Không tìm thấy giỏ hàng của người dùng.");
                    }
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
