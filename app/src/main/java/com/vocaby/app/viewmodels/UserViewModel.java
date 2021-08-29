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
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.repositories.VocabyRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserViewModel extends AndroidViewModel {
    private final VocabyRepository vocabyRepository;
    private MutableLiveData<User> mUser;
    private final MutableLiveData<List<String>> mSavedWords;
    private final String LOCAL_ID_KEY = "LOCAL_USER_ID";
    private final String CURRENT_ID_KEY = "CURRENT_USER_ID";
    private final CompositeDisposable compositeDisposable;
    SharedPreferences sharedPreferences;

    public UserViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        sharedPreferences = getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        mSavedWords = new MutableLiveData<>();
        compositeDisposable.add(
            vocabyRepository.getUser(sharedPreferences.getInt(CURRENT_ID_KEY, 1))
                .subscribe(user -> {
                        mUser = new MutableLiveData<>(user);
                        setSavedWords();
                    },
                    e -> {
                        if(e instanceof EmptyResultSetException) {
                            User user = new User();
                            compositeDisposable.add(
                                    vocabyRepository.createUser(user)
                                            .subscribe(id -> {
                                                int userId = id.intValue();
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putInt("LOCAL_USER_ID", userId);
                                                editor.putInt("CURRENT_USER_ID", userId);
                                                editor.apply();
                                                mUser = new MutableLiveData<>(user);
                                                setSavedWords();
                                            }, error -> Log.e("UserViewModel (new user): ", e.getMessage()))
                            );
                        } else {
                            Log.e("UserViewModel (current user): ", e.getMessage());
                        }
                    })
        );
    }

    public void loginUser() {
        compositeDisposable.add(
            vocabyRepository.getUser(sharedPreferences.getInt(CURRENT_ID_KEY, 0))
                .subscribe(user -> {
                        mUser.setValue(user);
                        setSavedWords();
                    },
                        Throwable::printStackTrace)
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
            vocabyRepository.getUserSaves(sharedPreferences.getInt(CURRENT_ID_KEY, 0))
                .subscribe(mSavedWords::setValue, error -> Log.e("UserViewModel (setSavedWords): ", error.getMessage()))
        );
    }

    public boolean hasSave(String word) {
        if(mUser.getValue() != null && mSavedWords.getValue() != null) {
            if(mUser.getValue().isLoggedIn()) {
                return false;
            } else {
                return mSavedWords.getValue().contains(word);
            }
        }

        return false;
    }

    public String getSaveItem(int position) {
        return mSavedWords.getValue().get(position);
    }

    public void refreshSaves() {
        Log.d("UserViewModel", "refreshSaves: ");
    }

    public void saveWord(String word) {
        if(mUser.getValue() != null && mSavedWords.getValue() != null) {
            if(mUser.getValue().isLoggedIn()) {
                // Add Locally
                compositeDisposable.add(
                    vocabyRepository.addSave(new UserSaves(mUser.getValue().getUserId(), word))
                        .subscribe(() -> {},
                                Throwable::printStackTrace)
                );
            } else {
               // Add Remote
            }
        }
    }

    public void removeSave(String word) {
        if(mUser.getValue() != null && mSavedWords.getValue() != null) {
            if(mUser.getValue().isLoggedIn()) {
                compositeDisposable.add(
                    vocabyRepository.removeSave(new UserSaves(mUser.getValue().getUserId(), word))
                            .subscribe(() -> {},
                                    Throwable::printStackTrace)
                );
            } else {
                // Remove Remote
            }
        }
    }

    public void logout() {
        int id = sharedPreferences.getInt(LOCAL_ID_KEY, 1);
        Completable deleteUser = vocabyRepository.deleteUser(mUser.getValue());
        Single<User> getUser = vocabyRepository.getUser(id);
        if(mUser.getValue() != null) {
            compositeDisposable.add(
                vocabyRepository.getVocabyApiService("")
                    .logout("Token " + mUser.getValue().getToken())
                    .andThen(deleteUser)
                    .andThen(getUser)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(user -> {
                        mUser.setValue(user);
                        setSavedWords();

                        // Set current user ID back to local user ID
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(CURRENT_ID_KEY, user.getUserId());
                        editor.apply();
                    }, Throwable::printStackTrace));
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
