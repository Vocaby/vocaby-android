package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.database.entity.User;
import com.vocaby.app.models.UserModel;
import com.vocaby.app.repositories.UserRepository;
import com.vocaby.app.repositories.VocabyRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserViewModel extends AndroidViewModel {
    private final VocabyRepository vocabyRepository;
    private MutableLiveData<Boolean> isLoggedIn;
    private MutableLiveData<User> mUser;
    private MutableLiveData<List<String>> mSavedWords;
    private final String ID_KEY = "USER_ID";
    private final CompositeDisposable compositeDisposable;
    SharedPreferences sharedPreferences;

    public UserViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        sharedPreferences = getApplication().getSharedPreferences(ID_KEY, Context.MODE_PRIVATE);
        mSavedWords = new MutableLiveData<>();

        compositeDisposable.add(
            vocabyRepository.getCurrentUser(sharedPreferences.getInt(ID_KEY, 0))
                .subscribe(user -> {
                        mUser = new MutableLiveData<>(user);
                        Log.d("UserViewModel: ", "Initializing user: " + user.getToken());
                        if(user.getToken().isEmpty()) {
                            isLoggedIn = new MutableLiveData<>(false);
                        } else {
                            isLoggedIn = new MutableLiveData<>(true);
                        }

                        setSavedWords();
                    },
                    error -> {
                        User user = new User();
                        if(error instanceof EmptyResultSetException) {
                            vocabyRepository.createUser(user)
                                .subscribe(id -> {
                                    Log.d("UserViewModel: ", "Creating new user");
                                    int userId = id.intValue();
                                    SharedPreferences sharedPreferences = getApplication().getSharedPreferences(ID_KEY, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt(ID_KEY, userId);
                                    editor.apply();
                                    mUser = new MutableLiveData<>(user);
                                    isLoggedIn = new MutableLiveData<>(false);

                                    setSavedWords();
                                },
                                error2 -> Log.d("UserViewModel: ", error2.getMessage()));
                        } else {
                            error.printStackTrace();
                        }

                    })
        );

        compositeDisposable.add(
                vocabyRepository.getUserCount()
                    .subscribe(count -> Log.d("UserViewModel: ", count + ""))
        );
    }

    public void loginUser() {
        compositeDisposable.add(
            vocabyRepository.getCurrentUser(sharedPreferences.getInt(ID_KEY, 0))
                .subscribe(user -> {
                        mUser.setValue(user);
                        isLoggedIn.setValue(true);
                        setSavedWords();
                    },
                    error -> {
                        error.printStackTrace();
                    })
        );
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<List<String>> getSavedWords() {
        return mSavedWords;
    }

    public void setSavedWords() {
        compositeDisposable.add(
            vocabyRepository.getUserSaves(sharedPreferences.getInt(ID_KEY, 0))
                .subscribe(list -> {
                    mSavedWords.setValue(list);
                    Log.d("setSavedWords: ", list.size() + "");
                }, error -> Log.e("UserViewModel (setSavedWords): ", error.getMessage()))
        );
    }

    public String getSaveItem(int position) {
        return mSavedWords.getValue().get(position);
    }

    public void logout() {
        if(mUser.getValue() != null) {
            compositeDisposable.add(
                vocabyRepository.getVocabyApiService("")
                    .logout("Token " + mUser.getValue().getToken())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        vocabyRepository.clearUser()
                                .subscribe(() -> {
                                    User user = new User();
                                    vocabyRepository.createUser(user)
                                            .subscribe(id -> {
                                                    int userId = id.intValue();
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    editor.putInt(ID_KEY, userId);
                                                    editor.apply();
                                                    mUser.setValue(user);
                                                    setSavedWords();
                                                },
                                                    error -> Log.e("logout (create): ", error.getMessage()));
                                }, error -> Log.e("logout (clear): ", error.getMessage()));
                    }, error -> Log.e("logout (api): ", error.getMessage()))
            );
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
