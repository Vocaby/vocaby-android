package com.vocaby.app.models;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class UserModel implements Serializable {
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private List<String> savedWords;

    public UserModel() {
        this.email = "guest@vocaby.app";
        this.username = "Guest";
        this.savedWords = new LinkedList<>();
        this.firstName = "";
        this.lastName = "";
    }

    public UserModel(String email, List<String> savedWords) {
        this.email = email;
        this.username = "";
        this.savedWords = savedWords;
        this.firstName = "";
        this.lastName = "";
    }

    public String getEmail() { return this.email; }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getSavedWords() { return this.savedWords; }

    public void setSavedWords(List<String> saves) { this.savedWords = saves; }
}
