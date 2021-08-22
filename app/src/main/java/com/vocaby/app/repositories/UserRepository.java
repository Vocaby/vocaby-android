package com.vocaby.app.repositories;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocaby.app.DataManager;
import com.vocaby.app.R;
import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.models.UserModel;
import com.vocaby.app.utils.SingleLiveEvent;

import java.io.IOException;
import java.util.List;

public class UserRepository {
    private DataManager dataManager;
    private ApiManager apiManager;
    private UserModel user;

    public UserRepository(Application application) {
        dataManager = DataManager.getInstance(application);
        apiManager = ApiManager.getInstance();
        user = dataManager.getUser();
    }

    public UserModel getUser() {
        user = dataManager.getUser();
        return user;
    }

    public void setUser(String email, List<String> savedWords) throws IOException {
        user = new UserModel(email, savedWords);
        dataManager.setUser(user);
    }

    public UserModel deleteUser() throws IOException {
        user = new UserModel();
        dataManager.setUser(user);
        return user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public List<String> getSaves() {
        return user.getSavedWords();
    }

    public VocabyApiService getVocabyLogoutService() {
        return apiManager.getVocabyApiService("");
    }
}
