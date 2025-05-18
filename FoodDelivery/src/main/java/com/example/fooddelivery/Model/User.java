package com.example.fooddelivery.Model;

import java.time.LocalDate;
import java.time.Period;

public class User {
    private int user_id;
    private String full_name;
    private String email;
    private String password_hash;
    private LocalDate date_of_birth;
    private String phone_number;
    private String gender;
    private String profile_picture_url;
    private String role;
    private String address;

    /**
     * Constructor không bao gồm địa chỉ.
     */
    public User(int user_id, String full_name, String email, String password_hash,
                LocalDate date_of_birth, String phone_number, String gender,
                String profile_picture_url, String role) {
        this.user_id = user_id;
        this.full_name = full_name;
        this.email = email;
        this.password_hash = password_hash;
        this.date_of_birth = date_of_birth;
        this.phone_number = phone_number;
        this.gender = gender;
        this.profile_picture_url = profile_picture_url;
        this.role = role;
        this.address = "";
    }

    /**
     * Constructor bao gồm đầy đủ thông tin.
     */
    public User(int user_id, String full_name, String email, String password_hash,
                LocalDate date_of_birth, String phone_number, String gender,
                String profile_picture_url, String role, String address) {
        this.user_id = user_id;
        this.full_name = full_name;
        this.email = email;
        this.password_hash = password_hash;
        this.date_of_birth = date_of_birth;
        this.phone_number = phone_number;
        this.gender = gender;
        this.profile_picture_url = profile_picture_url;
        this.role = role;
        this.address = (address != null) ? address : "";
    }

    // Constructor mặc định
    public User() {}

    // Getter
    public int getUser_id() { return user_id; }
    public String getFull_name() { return full_name; }
    public String getEmail() { return email; }
    public String getPassword_hash() { return password_hash; }
    public LocalDate getDate_of_birth() { return date_of_birth; }
    public String getPhone_number() { return phone_number; }
    public String getGender() { return gender; }
    public String getProfile_picture_url() { return profile_picture_url; }
    public String getRole() { return role; }
    public String getAddress() { return address; }

    // Setter
    public void setUser_id(int user_id) { this.user_id = user_id; }
    public void setFull_name(String full_name) { this.full_name = full_name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }
    public void setDate_of_birth(LocalDate date_of_birth) { this.date_of_birth = date_of_birth; }
    public void setPhone_number(String phone_number) { this.phone_number = phone_number; }
    public void setGender(String gender) { this.gender = gender; }
    public void setProfile_picture_url(String profile_picture_url) { this.profile_picture_url = profile_picture_url; }
    public void setRole(String role) { this.role = role; }
    public void setAddress(String address) { this.address = address; }

    /**
     * Tính toán tuổi của người dùng dựa trên ngày sinh.
     * @return Tuổi hoặc 0 nếu không có ngày sinh.
     */
    public int getAge() {
        if (this.date_of_birth != null) {
            return Period.between(this.date_of_birth, LocalDate.now()).getYears();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", full_name='" + full_name + '\'' +
                ", email='" + email + '\'' +
                ", date_of_birth=" + date_of_birth +
                ", phone_number='" + phone_number + '\'' +
                ", gender='" + gender + '\'' +
                ", profile_picture_url='" + profile_picture_url + '\'' +
                ", role='" + role + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
