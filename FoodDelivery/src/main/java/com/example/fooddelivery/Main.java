package com.example.fooddelivery;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    // Giữ tham chiếu stage chính để chuyển đổi giữa các giao diện
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showLoginView();  // Mặc định mở trang Login
    }

    /**
     * Hiển thị giao diện Login.fxml
     */
    public static void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("Food Delivery - Login");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Không thể load Login.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị giao diện Register.fxml
     */
    public static void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/Register.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("Food Delivery - Register");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Không thể load Register.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hiển thị giao diện ForgetPassword.fxml
     */
    public static void showForgetPasswordView() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/ForgetPassword.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("Food Delivery - ForgetPassword");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Không thể load ForgetPassword.fxml: " + e.getMessage());
            e.printStackTrace();
        }

    }public static void showAdminContainerView() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/Admin/AdminContainer.fxml"));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle("Food Delivery - Admin");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Không thể load AdminContainer.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
