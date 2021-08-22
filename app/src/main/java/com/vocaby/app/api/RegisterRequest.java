package com.vocaby.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RegisterRequest {
    @SerializedName("email")
    final String email;
    @SerializedName("password")
    final String password;
    @SerializedName("first_name")
    final String firstName;
    @SerializedName("last_name")
    final String lastName;
    @SerializedName("saves")
    final List<String> saves;

    public RegisterRequest(String email, String password, List<String> saves) {
        this.email = email;
        this.password = password;
        this.saves = saves;
        this.firstName = "";
        this.lastName = "";
    }
}
