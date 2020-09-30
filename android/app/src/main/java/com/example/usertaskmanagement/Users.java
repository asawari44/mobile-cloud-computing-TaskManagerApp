package com.example.usertaskmanagement;

public class Users {
    public String displayName, profilePictureUrl, uid;

    public Users(String uid, String name) {
        this.displayName = name;
        this.uid = uid;
    }

    public void setName(String name) {
        this.displayName = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }



    public String getName() {
        return displayName;
    }
    public String getUid() {
        return uid;
    }




}
