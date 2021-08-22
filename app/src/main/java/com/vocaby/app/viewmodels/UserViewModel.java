package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocaby.app.models.UserModel;
import com.vocaby.app.repositories.UserRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserViewModel extends AndroidViewModel {
    private final MutableLiveData<Boolean> isLoggedIn;
    private final MutableLiveData<UserModel> mUserModel;
    private final MutableLiveData<List<String>> mSavedWords;
    private final UserRepository userRepository;
    private CompositeDisposable compositeDisposable;
    SharedPreferences sharedPreferences;
    private final String TOKEN_KEY = "TOKEN";

    public UserViewModel(@NonNull Application application) {
        super(application);
        isLoggedIn = new MutableLiveData<>();
        userRepository = new UserRepository(application);
        sharedPreferences = application.getSharedPreferences(TOKEN_KEY, Context.MODE_PRIVATE);
        mUserModel = new MutableLiveData<>(userRepository.getUser());
        mSavedWords = new MutableLiveData<>(userRepository.getSaves());
        compositeDisposable = new CompositeDisposable();

        refreshLoginStatus();
    }

    public void refreshLoginStatus() {
        setLoginStatus(!sharedPreferences.getString(TOKEN_KEY, "").isEmpty());
    }

    public void refreshUser() {
        mUserModel.setValue(userRepository.getUser());
    }

    public LiveData<UserModel> getUser() {
        return mUserModel;
    }

    public LiveData<List<String>> getSavedWords() {
        return mSavedWords;
    }

    public String getSaveItem(int position) {
        return mSavedWords.getValue().get(position);
    }

    public String getUserName() {
        return userRepository.getUsername();
    }

    public String getEmail() {
        return userRepository.getEmail();
    }


    public LiveData<Boolean> getLoginStatus() {
        return isLoggedIn;
    }

    public void setLoginStatus(Boolean loggedIn) {
        isLoggedIn.setValue(loggedIn);
    }

    public void setToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public void logout() {
        if(isLoggedIn.getValue()) {
            String token = "Token " + sharedPreferences.getString(TOKEN_KEY, "");
            compositeDisposable.add(
                userRepository.getVocabyLogoutService().logout(token)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            isLoggedIn.setValue(false);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(TOKEN_KEY, "");
                            editor.apply();
                            UserModel guest = userRepository.deleteUser();
                            mUserModel.setValue(guest);
                            Log.d("Logout", "Successful");
                        }, throwable -> {
                            Log.d("Logout", throwable.getMessage());
                        })
            );
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
