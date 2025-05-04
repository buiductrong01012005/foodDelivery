package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Model.Food;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FoodItemController {

    @FXML private ImageView imgFood;
    @FXML private Label lblFoodName;
    @FXML private Button btnAddCart;

    private Food food;

    public void setData(Food food) {
        this.food = food;
        lblFoodName.setText(food.getName());

        try {
            imgFood.setImage(new Image(food.getImage_url(), true));
        } catch (Exception e) {
            System.out.println("Không load được ảnh: " + food.getImage_url());
        }

        btnAddCart.setOnAction(event -> openFoodDetail());
    }

    private void openFoodDetail() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User/UserFood.fxml"));
            Parent root = loader.load();

            FoodDetailController controller = loader.getController();
            controller.setFood(food);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết món ăn - " + food.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // chặn tương tác màn trước
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
