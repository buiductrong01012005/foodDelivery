package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Model.Meal;
import com.example.fooddelivery.Model.MealResponse;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class MealDetailController {

    @FXML private ImageView imageView;
    @FXML private Label nameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label areaLabel;
    @FXML private TextArea instructionsArea;

    public void loadMealDetail(String mealId) {
        new Thread(() -> {
            try {
                String urlStr = "https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + mealId;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String json = reader.lines().collect(Collectors.joining());

                Gson gson = new Gson();
                MealResponse response = gson.fromJson(json, MealResponse.class);
                Meal meal = response.getMeals().get(0);

                javafx.application.Platform.runLater(() -> {
                    nameLabel.setText(meal.getStrMeal());
                    categoryLabel.setText("Category: " + meal.getStrCategory());
                    areaLabel.setText("Area: " + meal.getStrArea());
                    instructionsArea.setText(meal.getStrInstructions());
                    imageView.setImage(new Image(meal.getStrMealThumb()));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
