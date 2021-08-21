package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocaby.app.R;
import com.vocaby.app.models.User;
import com.vocaby.app.repositories.UserRepository;

public class UserViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private MutableLiveData<User> mUser;

    public UserViewModel(@NonNull Application application) {
        super(application);
        mUser = new MutableLiveData<>();
        userRepository = new UserRepository(application);
        mUser.setValue(userRepository.getUser());
    }

    public LiveData<User> getUser() {
        return mUser;
    }

}
