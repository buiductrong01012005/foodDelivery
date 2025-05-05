package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Database.DatabaseConnector;
import com.example.fooddelivery.Model.Food;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CartController {

    @FXML private GridPane cartGrid;
    @FXML private Text txtSubtotal;
    @FXML private Text txtShipping;
    @FXML private Text txtTotal;
    @FXML private TextField txtSearch;
    @FXML private Button btnCheckout;

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
        if (btnCheckout != null) {
            btnCheckout.setOnAction(e -> handleCheckout());
        }
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

            Text name = new Text(food.getName());
            cartGrid.add(name, 0, row);

            Text priceText = new Text(String.format("%.0f VNĐ", price));
            cartGrid.add(priceText, 1, row);

            TextField qtyField = new TextField(String.valueOf(quantity));
            qtyField.setPrefWidth(50);
            cartGrid.add(qtyField, 2, row);

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

            Button deleteBtn = new Button("Xoá");
            deleteBtn.setOnAction(e -> {
                deleteCartItem(userId, food.getFood_id());
                loadCartItems();
            });
            cartGrid.add(deleteBtn, 4, row);

            row++;
        }

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

    private void handleCheckout() {
        List<CartItem> items = getCartItems(userId);

        if (items.isEmpty()) {
            showAlert("Giỏ hàng đang trống!");
            return;
        }

        double subtotal = items.stream().mapToDouble(i -> i.food.getPrice() * i.quantity).sum();
        double totalAmount = subtotal + SHIPPING_COST;

        int addressId = getDefaultAddressId(userId);
        if (addressId == -1) {
            showAlert("Không tìm thấy địa chỉ mặc định.");
            return;
        }

        String orderCode = generateOrderCode();

        try (Connection conn = DatabaseConnector.connectDB()) {
            conn.setAutoCommit(false);

            String insertOrder = """
                INSERT INTO orders (user_id, delivery_address_id, order_code,
                                    food_cost, delivery_fee, discount_amount, total_amount,
                                    delivery_method, order_status, payment_method, payment_status, placed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'Standard', 'Pending', 'COD', 'Pending', NOW())
            """;

            PreparedStatement orderStmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, userId);
            orderStmt.setInt(2, addressId);
            orderStmt.setString(3, orderCode);
            orderStmt.setDouble(4, subtotal);
            orderStmt.setDouble(5, SHIPPING_COST);
            orderStmt.setDouble(6, 0.0);
            orderStmt.setDouble(7, totalAmount);
            orderStmt.executeUpdate();

            ResultSet rs = orderStmt.getGeneratedKeys();
            if (!rs.next()) throw new SQLException("Không tạo được đơn hàng!");
            int orderId = rs.getInt(1);

            String insertItem = """
                INSERT INTO order_items (order_id, food_id, quantity, price_at_order, item_subtotal)
                VALUES (?, ?, ?, ?, ?)
            """;
            PreparedStatement itemStmt = conn.prepareStatement(insertItem);
            for (CartItem item : items) {
                double itemSubtotal = item.quantity * item.food.getPrice();
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.food.getFood_id());
                itemStmt.setInt(3, item.quantity);
                itemStmt.setDouble(4, item.food.getPrice());
                itemStmt.setDouble(5, itemSubtotal);
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();

            String deleteCart = """
                DELETE FROM cart_items
                WHERE cart_id = (SELECT cart_id FROM carts WHERE user_id = ?)
            """;
            PreparedStatement deleteStmt = conn.prepareStatement(deleteCart);
            deleteStmt.setInt(1, userId);
            deleteStmt.executeUpdate();

            conn.commit();
            showAlert("Đặt hàng thành công!\nMã đơn: " + orderCode);
            loadCartItems();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi đặt hàng: " + e.getMessage());
        }
    }

    private int getDefaultAddressId(int userId) {
        String sql = "SELECT address_id FROM addresses WHERE user_id = ? AND is_default = TRUE LIMIT 1";
        try (Connection conn = DatabaseConnector.connectDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("address_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private String generateOrderCode() {
        String cityCode = "HCM";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int random = 100 + new Random().nextInt(900);
        return "FD" + cityCode + date + random;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
