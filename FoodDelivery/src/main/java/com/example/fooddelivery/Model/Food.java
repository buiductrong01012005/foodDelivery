package com.example.fooddelivery.Model;

import java.time.LocalDateTime;

public class Food {
    private int food_id;
    private int category_id;
    private String name;
    private String description;
    private double price;
    private String availability_status;
    private String image_url;
    private int created_by;
    private int updated_by;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private String category_name;

    public Food() {
        this.category_name = "N/A"; // Giá trị mặc định
    }

    public Food(int food_id, int category_id, String name, String description, double price, String availability_status, String image_url, int created_by, int updated_by, LocalDateTime created_at, LocalDateTime updated_at) {
        this.food_id = food_id;
        this.category_id = category_id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.availability_status = availability_status;
        this.image_url = image_url;
        this.created_by = created_by;
        this.updated_by = updated_by;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Food(int food_id, int category_id,String category_name, String name, String description, double price, String availability_status, String image_url, int created_by, int updated_by, LocalDateTime created_at, LocalDateTime updated_at) {
        this.food_id = food_id;
        this.category_id = category_id;
        this.category_name = (category_name != null) ? category_name : "N/A";
        this.name = name;
        this.description = description;
        this.price = price;
        this.availability_status = availability_status;
        this.image_url = image_url;
        this.created_by = created_by;
        this.updated_by = updated_by;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getFood_id() {
        return food_id;
    }

    public void setFood_id(int food_id) {
        this.food_id = food_id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getAvailability_status() {
        return availability_status;
    }

    public void setAvailability_status(String availability_status) {
        this.availability_status = availability_status;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getCreated_by() {
        return created_by;
    }

    public void setCreated_by(int created_by) {
        this.created_by = created_by;
    }

    public int getUpdated_by() {
        return updated_by;
    }

    public void setUpdated_by(int updated_by) {
        this.updated_by = updated_by;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getImagePath() {
        return image_url;
    }

    public void setImagePath(String image_url) {
        this.image_url = image_url;
    }
}