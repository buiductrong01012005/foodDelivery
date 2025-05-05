package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.Food;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartController {

    @FXML private GridPane cartGrid;
    @FXML private Text txtSubtotal;
    @FXML private Text txtShipping;
    @FXML private Text txtTotal;
    @FXML private TextField txtSearch;

    private final int userId = 1;  // Giả lập ID người dùng đăng nhập
    private static final double SHIPPING_COST = 15000;

    private static class CartItem {
        Food food;
        int quantity;

        CartItem(Food food, int quantity) {
            this.food = food;
            this.quantity = quantity;
        }
    }

    @FXML
    public void initialize() {
        loadCartItems();
    }

    private void loadCartItems() {
        cartGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        List<CartItem> items = getCartItems(userId);
        double subtotal = 0;
        int row = 1;

        for (CartItem item : items) {
            Food food = item.food;
            int quantity = item.quantity;
            double price = food.getPrice();
            subtotal += price * quantity;

            // Tên món
            Text name = new Text(food.getName());
            cartGrid.add(name, 0, row);

            // Giá
            Text priceText = new Text(String.format("%.0f VNĐ", price));
            cartGrid.add(priceText, 1, row);

            // TextField Số lượng
            TextField qtyField = new TextField(String.valueOf(quantity));
            qtyField.setPrefWidth(50);
            cartGrid.add(qtyField, 2, row);

            // Nút cập nhật
            Button updateBtn = new Button("Cập nhật");
            updateBtn.setOnAction(e -> {
                try {
                    int newQty = Integer.parseInt(qtyField.getText());
                    if (newQty > 0) {
                        updateCartItem(userId, food.getFood_id(), newQty);
                        loadCartItems();
                    } else {
                        showAlert("Số lượng phải > 0");
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Số lượng không hợp lệ");
                }
            });
            cartGrid.add(updateBtn, 3, row);

            // Nút xoá
            Button deleteBtn = new Button("Xoá");
            deleteBtn.setOnAction(e -> {
                deleteCartItem(userId, food.getFood_id());
                loadCartItems();
            });
            cartGrid.add(deleteBtn, 4, row);

            row++;
        }

        // Hiển thị tổng cộng
        txtSubtotal.setText(String.format("%.0f VNĐ", subtotal));
        txtShipping.setText(String.format("%.0f VNĐ", SHIPPING_COST));
        txtTotal.setText(String.format("%.0f VNĐ", subtotal + SHIPPING_COST));
    }

    private List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = """
                SELECT f.*, ci.quantity
                FROM cart_items ci
                JOIN carts c ON c.cart_id = ci.cart_id
                JOIN foods f ON f.food_id = ci.food_id
                WHERE c.user_id = ?
                """;

        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Food food = new Food(
                        rs.getInt("food_id"),
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("availability_status"),
                        rs.getString("image_url"),
                        rs.getInt("created_by"),
                        rs.getInt("updated_by"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );
                int quantity = rs.getInt("quantity");
                items.add(new CartItem(food, quantity));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    private void updateCartItem(int userId, int foodId, int newQty) {
        String sql = """
            UPDATE cart_items
            SET quantity = ?
            WHERE cart_id = (SELECT cart_id FROM carts WHERE user_id = ?)
              AND food_id = ?
        """;

        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQty);
            stmt.setInt(2, userId);
            stmt.setInt(3, foodId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteCartItem(int userId, int foodId) {
        String sql = """
            DELETE FROM cart_items
            WHERE cart_id = (SELECT cart_id FROM carts WHERE user_id = ?)
              AND food_id = ?
        """;

        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, foodId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
