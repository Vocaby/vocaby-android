package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.LoginRequest;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.models.AuthModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class LoginViewModel extends AndroidViewModel {
    private final VocabyRepository vocabyRepository;
    private final SingleLiveEvent<AuthModel> mAuthModel;
    private final CompositeDisposable compositeDisposable;
    private final SingleLiveEvent<Boolean> mLoginSuccessful;
    private final String CURRENT_ID_KEY = "CURRENT_USER_ID";

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

    // Clean up later
    public void login() {
        String email = mAuthModel.getValue().getEmail();
        String password = mAuthModel.getValue().getPassword();
        SharedPreferences sharedPreferences =
                getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        compositeDisposable.add(
            vocabyRepository.getUserSaves(sharedPreferences.getInt(CURRENT_ID_KEY, 0))
                .flatMap(list -> {
                    LoginRequest loginRequest = new LoginRequest(email, password, list);
                    return ApiManager.getInstance().getVocabyApiService("L").login(loginRequest)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                }).flatMap(authResponse -> vocabyRepository.createUser(
                        new User(
                            email,
                            "Eric",
                            "Kim",
                            authResponse.getToken()
                        )
                    ).flatMap(id -> {
                                int userId = id.intValue();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(CURRENT_ID_KEY, userId);
                                editor.apply();

                                List<String> s = authResponse.getSaves();
                                List<UserSaves> userSaves = new LinkedList<>();
                                for(String word : s) {
                                    userSaves.add(new UserSaves(userId, word));
                                }

                                return vocabyRepository.insertSavedWords(userSaves);
                        })
                ).subscribe(saves -> mLoginSuccessful.setValue(true), error -> {
                    if(!(error instanceof HttpException)) {
                        Bugsnag.notify(error);
                        Log.e("UserViewModel (login): ", error.getMessage());
                    }
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
