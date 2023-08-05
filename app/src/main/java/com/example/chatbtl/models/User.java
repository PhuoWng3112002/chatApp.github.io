package com.example.chatbtl.models;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    private String id, name, phone, image, password;
    private Boolean isOnline;
    private List<String> friendIds;

    public User() {
    }

    public User(String name, String phone, String image, String password, List<String> friendIds) {
        this.name = name;
        this.phone = phone;
        this.image = image;
        this.password = password;
        this.friendIds = friendIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline(Boolean online) {
        isOnline = online;
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }
}
