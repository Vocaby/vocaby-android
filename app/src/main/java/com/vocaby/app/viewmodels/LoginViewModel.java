package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.LoginRequest;
import com.vocaby.app.models.AuthModel;
import com.vocaby.app.repositories.UserRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginViewModel extends AndroidViewModel {
    private final SingleLiveEvent<AuthModel> mAuthModel;
    private final CompositeDisposable compositeDisposable;
    private final SingleLiveEvent<Boolean> mLoginSuccessful;
    private final String TOKEN_KEY = "TOKEN";
    SharedPreferences sharedPreferences;

    public LoginViewModel(Application application) {
        super(application);
        mAuthModel = new SingleLiveEvent<>();
        mLoginSuccessful = new SingleLiveEvent<>();
        compositeDisposable = new CompositeDisposable();
        sharedPreferences = application.getSharedPreferences(TOKEN_KEY, Context.MODE_PRIVATE);
    }

    public LiveData<AuthModel> getAuthModel() {
        return mAuthModel;
    }

    public void setLoginData(String email, String password) {
        mAuthModel.setValue(new AuthModel(email, password));
    }

    public void login() {
        String email = mAuthModel.getValue().getEmail();
        String password = mAuthModel.getValue().getPassword();
        UserRepository userRepository = new UserRepository(getApplication());
        List<String> saves = userRepository.getSaves();
        LoginRequest loginRequest = new LoginRequest(email, password, saves);
        compositeDisposable.add(
            ApiManager.getInstance().getVocabyApiService("AUTH").login(loginRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginResponse -> {
                    if(loginResponse != null) {
                        mLoginSuccessful.setValue(true);
                        userRepository.setUser(email, loginResponse.getSaves());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(TOKEN_KEY, loginResponse.getToken());
                        editor.apply();
                    }
                }, error -> {
                    mLoginSuccessful.setValue(false);
                    Log.d("Login Failed", error.getMessage());
                })
        );
    }

    public LiveData<Boolean> getLoginStatus() {
        return mLoginSuccessful;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
