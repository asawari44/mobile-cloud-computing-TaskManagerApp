package com.example.usertaskmanagement;

class Pictures {
    String createdTime;
    String type;
    String url;

    public Pictures(String createdTime, String type, String url) {
        this.createdTime = createdTime;
        this.type = type;
        this.url = url;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
