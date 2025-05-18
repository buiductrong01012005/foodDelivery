package com.example.fooddelivery.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class SidebarController {

    //@FXML private Button homeBtn;
    @FXML private Button personalBtn;
    @FXML private Button historyBtn;
    @FXML private Button logoutBtn;
    @FXML private Button homeBtn1;  // Thông tin
    //@FXML private Button homeBtn11; // Đăng xuất

    @FXML
    private void initialize() {
        homeBtn1.setOnAction(e -> loadView("/fxml/User/UserHome.fxml"));
        personalBtn.setOnAction(e -> loadView("/fxml/User/UserShoppingCart.fxml"));
        historyBtn.setOnAction(e -> loadView("/fxml/User/UserInformation.fxml"));
        logoutBtn.setOnAction(e -> loadView("/fxml/Login.fxml"));
        //homeBtn1.setOnAction(e -> loadView("/fxml/User/UserInformation.fxml")); // hoặc một view khác nếu có
        //homeBtn11.setOnAction(e -> logoutToLogin()); // Đăng xuất
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newView = loader.load();

            // Lấy stage hiện tại
            Stage stage = (Stage) homeBtn1.getScene().getWindow();

            // Tạo scene mới (nếu bạn dùng BorderPane, bạn có thể chỉ thay phần center)
            Scene scene = new Scene(newView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logoutToLogin() {
        loadView("/fxml/Login.fxml"); // Sửa đúng path nếu login nằm chỗ khác
    }
}
