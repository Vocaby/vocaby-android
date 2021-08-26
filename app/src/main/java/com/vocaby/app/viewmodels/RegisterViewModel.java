package com.vocaby.app.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.RegisterRequest;
import com.vocaby.app.models.AuthModel;
import com.vocaby.app.repositories.UserRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RegisterViewModel extends AndroidViewModel {
    private final SingleLiveEvent<AuthModel> mAuthModel;
    private final SingleLiveEvent<Boolean> mRegistrationSuccessful;
    private final CompositeDisposable compositeDisposable;

    public RegisterViewModel(Application application) {
        super(application);
        mAuthModel = new SingleLiveEvent<>();
        mRegistrationSuccessful = new SingleLiveEvent<>();
        compositeDisposable = new CompositeDisposable();
    }

    public LiveData<AuthModel> getAuthModel() {
        return mAuthModel;
    }

    public void setAuthData(String email, String password, String confirmationPassword) {
        mAuthModel.setValue(new AuthModel(email, password, confirmationPassword));
    }

    public void register() {
        String email = mAuthModel.getValue().getEmail();
        String password = mAuthModel.getValue().getPassword();
        UserRepository userRepository = new UserRepository(getApplication());
        List<String> saves = userRepository.getSaves();
        RegisterRequest registerRequest = new RegisterRequest(email, password, saves);
        compositeDisposable.add(
            ApiManager.getInstance().getVocabyApiService("").register(registerRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(authResponse -> {
                    mRegistrationSuccessful.setValue(true);
                }, error-> {
                    mRegistrationSuccessful.setValue(false);
                    Log.e("Registration Failed", error.getMessage());
                })
        );
    }

    public LiveData<Boolean> getRegistrationStatus() {
        return mRegistrationSuccessful;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
