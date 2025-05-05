package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Model.Food;
import com.example.fooddelivery.Dao.FoodDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class UserHomeController {

    @FXML
    private GridPane gridPane;

    public void initialize() {
        List<Food> foods = FoodDAO.getAllFoods();

        int col = 0, row = 0;
        for (Food food : foods) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User/FoodItem.fxml"));
                Node foodNode = loader.load();

                FoodItemController controller = loader.getController();

                controller.setData(food, f -> openFoodDetail(f)); // truyền callback

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

    private void openFoodDetail(Food food) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User/UserFood.fxml"));
            Parent root = loader.load();

            FoodDetailController controller = loader.getController();
            controller.setFood(food);

            Stage stage = new Stage();
            stage.setTitle("Chi tiết món ăn");
            stage.setScene(new Scene(root));
            stage.initOwner(gridPane.getScene().getWindow()); // để đảm bảo hiển thị trên top
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
