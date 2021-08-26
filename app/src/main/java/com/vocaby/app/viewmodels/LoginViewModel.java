package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.LoginRequest;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.models.AuthModel;
import com.vocaby.app.repositories.UserRepository;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginViewModel extends AndroidViewModel {
    private final VocabyRepository vocabyRepository;
    private final SingleLiveEvent<AuthModel> mAuthModel;
    private final CompositeDisposable compositeDisposable;
    private final SingleLiveEvent<Boolean> mLoginSuccessful;
    private final String ID_KEY = "USER_ID";
    SharedPreferences sharedPreferences;

    public LoginViewModel(Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        mAuthModel = new SingleLiveEvent<>();
        mLoginSuccessful = new SingleLiveEvent<>();
        compositeDisposable = new CompositeDisposable();
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
            ApiManager.getInstance().getVocabyApiService("LOGIN").login(loginRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(loginResponse -> {
                    if(loginResponse != null) {
                        vocabyRepository.clearUser()
                            .subscribe(() -> {
                                vocabyRepository.createUser(new User(
                                    email,
                                    "Eric",
                                    "Kim",
                                    loginResponse.getToken()
                                )).subscribe(id -> {
                                    int userId = id.intValue();
                                    SharedPreferences sharedPreferences = getApplication().getSharedPreferences(ID_KEY, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt(ID_KEY, userId);
                                    editor.apply();

                                    List<String> s = loginResponse.getSaves();
                                    List<UserSaves> userSaves = new LinkedList<>();
                                    for(String word : s) {
                                        userSaves.add(new UserSaves(userId, word));
                                    }

                                    vocabyRepository.insertSavedWords(userSaves)
                                        .subscribe(list -> {
                                            mLoginSuccessful.setValue(true);
                                        }, error -> Log.e("login (saves): ", error.getMessage()));
                                },
                                error -> Log.e("login (create): ", error.getMessage()));
                            },
                            error -> Log.e("login (clear): ", error.getMessage())
                        );
                    }
                }, error -> {
                    mLoginSuccessful.setValue(false);
                    Log.e("login (api):", error.getMessage());
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
