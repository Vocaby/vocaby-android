package com.vocaby.app.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AuthResponse {
    @SerializedName("token")
    final String token;
    @SerializedName("saves")
    final List<String> saves;

    AuthResponse(String token, List<String> saves) {
        this.token = token;
        this.saves = saves;
    }

    public String getToken() {
        return this.token;
    }

    public List<String> getSaves() {
        return this.saves;
    }
}
