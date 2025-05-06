package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Model.OrderDisplay;
import com.example.fooddelivery.Database.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminManageOrderController {

    @FXML private TableView<OrderDisplay> orderTableView;
    @FXML private TableColumn<OrderDisplay, Integer> idColumn;
    @FXML private TableColumn<OrderDisplay, String> foodItemsColumn;
    @FXML private TableColumn<OrderDisplay, Double> priceColumn;
    @FXML private TableColumn<OrderDisplay, String> notesColumn;
    @FXML private TableColumn<OrderDisplay, String> addressColumn;
    @FXML private TableColumn<OrderDisplay, String> statusColumn;
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private Button deleteButton;

    private ObservableList<OrderDisplay> allOrdersData = FXCollections.observableArrayList();
    private ObservableList<OrderDisplay> filteredOrdersData = FXCollections.observableArrayList();

    private Label noContentLabel;
    private Label noMatchingContentLabel;

    private DatabaseConnector dbConnector;


    @FXML
    public void initialize() {
        noContentLabel = new Label("Không có dữ liệu trong bảng");
        noMatchingContentLabel = new Label("Không tìm thấy đơn hàng nào phù hợp");

        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        foodItemsColumn.setCellValueFactory(cellData -> cellData.getValue().foodItemsProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        notesColumn.setCellValueFactory(cellData -> cellData.getValue().notesProperty());
        addressColumn.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        orderTableView.setItems(filteredOrdersData);
        updatePlaceholder();

        loadOrderData();

        searchButton.setOnAction(event -> filterOrders(searchTextField.getText()));
        searchTextField.setOnAction(event -> filterOrders(searchTextField.getText())); // Tìm kiếm khi nhấn Enter

        deleteButton.setOnAction(event -> handleDeleteOrder());
    }

    void loadOrderData() {
        allOrdersData.clear();
        String sql = "SELECT o.order_id, o.total_amount, o.order_status, o.special_instructions, " +
                "a.street_address, a.ward, a.district, a.city " +
                "FROM orders o " +
                "JOIN addresses a ON o.delivery_address_id = a.address_id " +
                "ORDER BY o.order_id DESC";

        try (Connection conn = dbConnector.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                double totalAmount = rs.getDouble("total_amount");
                String status = rs.getString("order_status");
                String notes = rs.getString("special_instructions");
                if (notes == null) {
                    notes = "";
                }
                String street = rs.getString("street_address");
                String ward = rs.getString("ward");
                String district = rs.getString("district");
                String city = rs.getString("city");

                String fullAddress = street;
                if (ward != null && !ward.isEmpty() && !ward.equalsIgnoreCase("NULL")) fullAddress += ", P. " + ward;
                if (district != null && !district.isEmpty() && !district.equalsIgnoreCase("NULL")) fullAddress += ", Q. " + district;
                if (city != null && !city.isEmpty() && !city.equalsIgnoreCase("NULL")) fullAddress += ", " + city;


                String foodItems = getFoodItemsForOrder(conn, orderId);

                allOrdersData.add(new OrderDisplay(orderId, foodItems, totalAmount, notes, fullAddress, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Cơ sở dữ liệu", "Không thể tải dữ liệu đơn hàng: " + e.getMessage());
        }
        filteredOrdersData.setAll(allOrdersData);
        updatePlaceholder();
    }

    private String getFoodItemsForOrder(Connection conn, int orderId) throws SQLException {
        StringBuilder itemsBuilder = new StringBuilder();
        String sql = "SELECT f.name, oi.quantity " +
                "FROM order_items oi " +
                "JOIN foods f ON oi.food_id = f.food_id " +
                "WHERE oi.order_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean firstItem = true;
                while (rs.next()) {
                    if (!firstItem) {
                        itemsBuilder.append(", ");
                    }
                    itemsBuilder.append(rs.getInt("quantity"))
                            .append(" ")
                            .append(rs.getString("name"));
                    firstItem = false;
                }
            }
        }
        return itemsBuilder.toString();
    }

    private void filterOrders(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            filteredOrdersData.setAll(allOrdersData);
        } else {
            String lowerCaseFilter = searchTerm.toLowerCase();
            filteredOrdersData.setAll(
                    allOrdersData.stream()
                            .filter(order -> {
                                boolean matchesId = false;
                                try {
                                    matchesId = order.getId() == Integer.parseInt(searchTerm.trim());
                                } catch (NumberFormatException e) {
                                    // Không phải số, bỏ qua việc khớp ID dựa trên số chính xác
                                }
                                return matchesId ||
                                        String.valueOf(order.getId()).contains(searchTerm.trim()) || // Cho phép khớp ID từng phần dưới dạng chuỗi
                                        order.getFoodItems().toLowerCase().contains(lowerCaseFilter) ||
                                        (order.getNotes() != null && order.getNotes().toLowerCase().contains(lowerCaseFilter)) || // Thêm tìm kiếm theo Ghi chú
                                        order.getStatus().toLowerCase().contains(lowerCaseFilter);
                            })
                            .collect(Collectors.toList())
            );
        }
        updatePlaceholder();
    }

    private void updatePlaceholder() {
        if (allOrdersData.isEmpty()) {
            orderTableView.setPlaceholder(noContentLabel);
        } else if (filteredOrdersData.isEmpty() && !searchTextField.getText().isEmpty()) {
            noMatchingContentLabel.setText("Không tìm thấy đơn hàng nào cho: \"" + searchTextField.getText() + "\"");
            orderTableView.setPlaceholder(noMatchingContentLabel);
        } else if (filteredOrdersData.isEmpty() && searchTextField.getText().isEmpty()){
            orderTableView.setPlaceholder(noContentLabel);
        }
        // Nếu filteredOrdersData không trống, nội dung bảng sẽ hiển thị, placeholder bị bỏ qua.
    }


    private void handleDeleteOrder() {
        OrderDisplay selectedOrder = orderTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn đơn hàng", "Vui lòng chọn một đơn hàng để xoá.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Xác nhận Xoá");
        confirmationAlert.setHeaderText("Xoá Đơn hàng ID: " + selectedOrder.getId());
        confirmationAlert.setContentText("Bạn có chắc chắn muốn xoá đơn hàng này không? Hành động này không thể hoàn tác.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = dbConnector.connectDB()) {
                String deleteOrderSql = "DELETE FROM orders WHERE order_id = ?";
                try (PreparedStatement pstmtOrder = conn.prepareStatement(deleteOrderSql)) {
                    pstmtOrder.setInt(1, selectedOrder.getId());
                    int affectedRows = pstmtOrder.executeUpdate();

                    if (affectedRows > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đơn hàng đã được xoá thành công.");
                        allOrdersData.remove(selectedOrder);
                        filteredOrdersData.remove(selectedOrder);
                        updatePlaceholder();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi Xoá", "Không thể xoá đơn hàng (không tìm thấy hoặc có lỗi).");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi Cơ sở dữ liệu", "Không thể xoá đơn hàng: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}