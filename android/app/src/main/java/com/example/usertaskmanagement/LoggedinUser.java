package com.example.usertaskmanagement;

import android.graphics.Bitmap;

public class LoggedinUser {
    private String userName;
    private String password;
    private String email;
    private Bitmap profileImage;
    //... other data fields that may be accessible to the UI

    LoggedinUser(String userName, String password, String email, Bitmap profileImage) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
    }

    String getUserName() {
        return userName;
    }
    String getPassword() {
        return password;
    }
    String getEmail() {
        return email;
    }
    Bitmap getProfileImage() {
        return profileImage;
    }
}
