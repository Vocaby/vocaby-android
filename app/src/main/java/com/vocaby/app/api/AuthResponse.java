package com.vocaby.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AuthResponse {
    @SerializedName("token")
    final String token;
    @SerializedName("saves")
    final List<String> saves;
    @SerializedName("first_name")
    final String firstName;
    @SerializedName("last_name")
    final String lastName;

    AuthResponse(String token, List<String> saves, String firstName, String lastName) {
        this.token = token;
        this.saves = saves;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getToken() {
        return this.token;
    }

    public List<String> getSaves() {
        return this.saves;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
