package com.example.fooddelivery.Controller;

import com.example.fooddelivery.Dao.FoodDao;
import com.example.fooddelivery.Model.Food;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class UserHomeController {

    @FXML
    private GridPane gridPane; // ánh xạ với fx:id trong UserHome.fxml

    public void initialize() {
        List<Food> foods = FoodDao.getAllFoods();

        int col = 0, row = 0;
        for (Food food : foods) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/User/FoodItem.fxml"));
                VBox foodBox = loader.load();

                FoodItemController controller = loader.getController();
                controller.setData(food);

                gridPane.add(foodBox, col, row);
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
}
