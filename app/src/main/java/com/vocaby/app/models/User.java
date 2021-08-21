package com.vocaby.app.models;

import java.util.LinkedList;
import java.util.List;

public class User {
    private String email;
    private String username;
    private List<Word> savedWords;
    private Boolean isLoggedIn;

    public User() {
        email = "guest@vocaby.app";
        username = "Guest";
        savedWords = new LinkedList<>();
        isLoggedIn = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
