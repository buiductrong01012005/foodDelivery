package com.example.fooddelivery.Model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OrderDisplay {
    private final IntegerProperty id;
    private final StringProperty foodItems;
    private final DoubleProperty price;
    private final StringProperty notes; // Thêm Ghi chú
    private final StringProperty address;
    private final StringProperty status;

    public OrderDisplay(int id, String foodItems, double price, String notes, String address, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.foodItems = new SimpleStringProperty(foodItems);
        this.price = new SimpleDoubleProperty(price);
        this.notes = new SimpleStringProperty(notes);
        this.address = new SimpleStringProperty(address);
        this.status = new SimpleStringProperty(status);
    }

    // ID
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    // FoodItems
    public String getFoodItems() { return foodItems.get(); }
    public StringProperty foodItemsProperty() { return foodItems; }

    // Price
    public double getPrice() { return price.get(); }
    public DoubleProperty priceProperty() { return price; }

    // Notes
    public String getNotes() { return notes.get(); }
    public StringProperty notesProperty() { return notes; }

    // Address
    public String getAddress() { return address.get(); }
    public StringProperty addressProperty() { return address; }

    // Status
    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
}