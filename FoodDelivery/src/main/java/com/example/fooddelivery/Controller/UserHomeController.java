package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Model.Food;
import com.example.fooddelivery.Dao.FoodDAO;
import com.example.fooddelivery.Model.Meal;
import com.example.fooddelivery.Model.MealResponse;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;
import java.util.stream.Collectors;

public class UserHomeController {

    @FXML private TextField searchField;
    @FXML private GridPane gridPane;

    public void initialize() {
        // Gán sự kiện tìm kiếm
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty()) {
                searchMeal(newText.trim());
            } else {
                loadLocalFoods(); // Reset lại danh sách nếu bỏ tìm
            }
        });

        loadLocalFoods(); // Mặc định load tất cả món từ DB
    }

    private void loadLocalFoods() {
        gridPane.getChildren().clear();
        List<Food> foods = FoodDAO.getAllFoods();

        int col = 0, row = 0;
        for (Food food : foods) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User/FoodItem.fxml"));
                Node foodNode = loader.load();

                FoodItemController controller = loader.getController();
                controller.setData(food, f -> openFoodDetail(f));

                gridPane.add(foodNode, col, row);
                col++;
                if (col == 5) {
                    col = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void searchMeal(String keyword) {
        new Thread(() -> {
            try {
                String urlStr = "https://www.themealdb.com/api/json/v1/1/search.php?s=" + URLEncoder.encode(keyword, "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String json = reader.lines().collect(Collectors.joining());
                reader.close();

                Gson gson = new Gson();
                MealResponse response = gson.fromJson(json, MealResponse.class);

                Platform.runLater(() -> displayMeals(response.getMeals()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayMeals(List<Meal> meals) {
        gridPane.getChildren().clear();
        if (meals == null) return;

        int col = 0, row = 0;
        for (Meal meal : meals) {
            VBox mealBox = createMealBox(meal);
            gridPane.add(mealBox, col++, row);
            if (col == 5) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createMealBox(Meal meal) {
        VBox box = new VBox(5);
        box.setStyle("-fx-border-color: #ddd; -fx-background-color: #fff; -fx-padding: 10px;");

        ImageView img = new ImageView(new Image(meal.getStrMealThumb(), 150, 100, true, true));
        Label name = new Label(meal.getStrMeal());
        name.setWrapText(true);
        name.setStyle("-fx-font-weight: bold");

        Button add = new Button("Xem chi tiết");
        add.setOnAction(e -> openMealDetail(meal.getIdMeal())); // ✅ gọi chi tiết món ăn

        box.getChildren().addAll(img, name, add);
        return box;
    }

    private void openMealDetail(String mealId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User/MealDetail.fxml"));
            Parent root = loader.load();

            MealDetailController controller = loader.getController();
            controller.loadMealDetail(mealId);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết món ăn");
            stage.setScene(new Scene(root));
            stage.initOwner(gridPane.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFoodDetail(Food food) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User/UserFood.fxml"));
            Parent root = loader.load();

            FoodDetailController controller = loader.getController();
            controller.setFood(food);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết món ăn (local)");
            stage.setScene(new Scene(root));
            stage.initOwner(gridPane.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
