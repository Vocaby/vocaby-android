package com.vocaby.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LoginRequest {
    @SerializedName("email")
    final String email;
    @SerializedName("password")
    final String password;
    @SerializedName("saves")
    final List<String> saves;

    public LoginRequest(String email, String password, List<String> saves) {
        this.email = email;
        this.password = password;
        this.saves = saves;
    }
}
