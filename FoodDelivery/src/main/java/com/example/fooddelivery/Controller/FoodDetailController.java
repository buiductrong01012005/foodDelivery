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

public class FoodDetailController {

    @FXML private ImageView imgFood;
    @FXML private Text txtName, txtStore, txtAddress, txtStatus, txtPrice;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtQuantity;
    @FXML private Button btnAddCart;
    @FXML private Button btnPlus, btnMinus;

    private Food food;
    private int userId = 1; // giả định user đang đăng nhập có id = 1

    public void setFood(Food food) {
        this.food = food;

        // Hiển thị dữ liệu món ăn
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

        // Nút tăng số lượng
        btnPlus.setOnAction(e -> {
            int current = Integer.parseInt(txtQuantity.getText());
            txtQuantity.setText(String.valueOf(current + 1));
        });

        // Nút giảm số lượng
        btnMinus.setOnAction(e -> {
            int current = Integer.parseInt(txtQuantity.getText());
            if (current > 1) txtQuantity.setText(String.valueOf(current - 1));
        });

        // Nút thêm vào giỏ
        btnAddCart.setOnAction(e -> {
            int quantity = 1;
            try {
                quantity = Integer.parseInt(txtQuantity.getText());
            } catch (NumberFormatException ex) {
                System.out.println("Số lượng không hợp lệ");
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

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("Đã thêm vào giỏ hàng thành công!");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Lỗi!");
                alert.setContentText("Không thể thêm vào giỏ hàng.");
                alert.showAndWait();
            }
        });
    }
}
