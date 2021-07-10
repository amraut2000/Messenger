package com.example.messenger.ajay.Models;

public class User {

    private String userId,name,profileImage,mobileNumber;

    public User(String userId, String name, String profileImage, String mobileNumber) {
        this.userId = userId;
        this.name = name;
        this.profileImage = profileImage;
        this.mobileNumber = mobileNumber;
    }

    public User() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }


}
