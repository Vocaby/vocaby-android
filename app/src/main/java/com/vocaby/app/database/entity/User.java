package com.vocaby.app.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.LinkedList;
import java.util.List;

@Entity(tableName = "vocaby_user")
public class User {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private int userId;

    private String email;
    private String firstName;
    private String lastName;
    private String token;

    public User() {
        this.email = "Guest";
        this.firstName = "";
        this.lastName = "";
        this.token = "";
    }

    public User(String email, String firstName, String lastName, String token) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getToken() { return this.token; }

    public void setToken(String token) { this.token = token; }

    public boolean isLoggedIn() { return !token.isEmpty(); }
}