package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.RegisterRequest;
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.models.AuthModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Response;

public class RegisterViewModel extends AndroidViewModel {
    private final SingleLiveEvent<AuthModel> mAuthModel;
    private final SingleLiveEvent<String> mRegistrationMessage;
    private final CompositeDisposable compositeDisposable;
    private final VocabyRepository vocabyRepository;

    public RegisterViewModel(Application application) {
        super(application);
        mAuthModel = new SingleLiveEvent<>();
        mRegistrationMessage = new SingleLiveEvent<>();
        compositeDisposable = new CompositeDisposable();
        vocabyRepository = new VocabyRepository(application);
    }

    public LiveData<AuthModel> getAuthModel() {
        return mAuthModel;
    }

    public void setAuthData(String email, String password, String confirmationPassword, String firstName, String lastName) {
        mAuthModel.setValue(new AuthModel(email, password, confirmationPassword, firstName, lastName));
    }

    public void register() {
        if(mAuthModel.getValue() != null) {
            String email = mAuthModel.getValue().getEmail();
            String password = mAuthModel.getValue().getPassword();
            String firstName = mAuthModel.getValue().getFirstName();
            String lastName = mAuthModel.getValue().getLastName();

            SharedPreferences sharedPreferences =
                    getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);

            compositeDisposable.add(
                    vocabyRepository.getUserSaves(sharedPreferences.getInt("CURRENT_USER_ID", 1))
                        .flatMap(list -> ApiManager.getInstance().getVocabyApiService(VocabyApiService.DEFAULT)
                                .register(new RegisterRequest(email, password, firstName, lastName, list))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(authResponse -> mRegistrationMessage.setValue("s"),
                            error-> {
                                if(error instanceof HttpException) {
                                    Response<?> response = ((HttpException) error).response();
                                    if(response != null) {
                                        int code = response.code();
                                        if(code == 403) {
                                            mRegistrationMessage.setValue("User already exists");
                                        } else if(code == 400) {
                                            mRegistrationMessage.setValue("Something went wrong on Vocaby's side. Please try again.");
                                        }
                                    }
                                } else {
                                    Bugsnag.notify(error);
                                }

                                Log.e("UserViewModel (login): ", error.getMessage());
                        })
            );
        }
    }

    public LiveData<String> getRegistrationStatus() {
        return mRegistrationMessage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
