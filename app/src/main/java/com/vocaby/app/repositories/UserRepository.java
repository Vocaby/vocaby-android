package com.vocaby.app.repositories;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.vocaby.app.models.User;

public class UserRepository {
    private String token;

    public UserRepository(Application application) {
        SharedPreferences sharedPref = application.getSharedPreferences("token", Context.MODE_PRIVATE);
        token = sharedPref.getString("token", "");
    }

    public String getUserToken() {
        return token;
    }

    public User getUser() {
        if(token.isEmpty()) {
            return new User();
        } else {
            // get User data from Shared Preferences
            return null;
        }
    }
}
