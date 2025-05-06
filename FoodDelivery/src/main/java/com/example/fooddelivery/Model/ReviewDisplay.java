package com.example.fooddelivery.Model;

import java.time.LocalDateTime;

public class ReviewDisplay {
    private int reviewId;
    private int userId;
    private String userName;
    private String userEmail;
    private String userPhoneNumber;
    private String reviewComment;
    private int rating;
    private LocalDateTime reviewDate;

    public ReviewDisplay(int reviewId, int userId, String userName, String userEmail, String userPhoneNumber, String reviewComment, int rating, LocalDateTime reviewDate) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhoneNumber = userPhoneNumber;
        this.reviewComment = reviewComment;
        this.rating = rating;
        this.reviewDate = reviewDate;
    }

    // Getters (matching PropertyValueFactory strings)
    public int getReviewId() { return reviewId; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserPhoneNumber() { return userPhoneNumber; }
    public String getReviewComment() { return reviewComment; }
    public int getRating() { return rating; }
    public LocalDateTime getReviewDate() { return reviewDate; }
}