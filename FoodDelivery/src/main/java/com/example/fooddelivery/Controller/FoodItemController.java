package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Model.Food;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.function.Consumer;

public class FoodItemController {

    @FXML private ImageView imgFood;
    @FXML private Label lblFoodName;
    @FXML private Button btnAddCart;

    private Food food;
    private Consumer<Food> onClick;

    public void setData(Food food, Consumer<Food> onClick) {
        this.food = food;
        this.onClick = onClick;
        lblFoodName.setText(food.getName());

        try {
            imgFood.setImage(new Image(food.getImage_url(), true));
        } catch (Exception e) {
            System.out.println("Không load được ảnh: " + food.getImage_url());
        }

        btnAddCart.setOnAction(event -> {
            if (onClick != null) {
                onClick.accept(food); // gọi về controller chính để mở chi tiết
            }
        });
    }
}
