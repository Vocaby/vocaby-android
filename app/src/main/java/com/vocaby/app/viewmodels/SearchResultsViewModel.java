package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.models.WordDataPackage;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class SearchResultsViewModel extends AndroidViewModel {
    private final CompositeDisposable compositeDisposable;
    private final VocabyRepository vocabyRepository;
    private final SingleLiveEvent<WordDataPackage> mWordPackage;
    private final SharedPreferences sharedPreferences;
    private final SingleLiveEvent<Boolean> remoteSaveSuccessful;
    private User currentUser;

    public SearchResultsViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        compositeDisposable = new CompositeDisposable();
        vocabyRepository = new VocabyRepository(application);
        mWordPackage = new SingleLiveEvent<>();
        remoteSaveSuccessful = new SingleLiveEvent<>();
    }

    public LiveData<WordDataPackage> getWordData() {
        return mWordPackage;
    }

    public LiveData<Boolean> getRemoteSaveStatus() { return remoteSaveSuccessful; }

    public void retrieveWordDataFromRepo(String searched, Boolean isConnected) {
        int userId = sharedPreferences.getInt("CURRENT_USER_ID", 1);
        VocabyApiService vocabyApi = vocabyRepository.getVocabyApiService("D");

        compositeDisposable.add(
            vocabyRepository.getUser(userId)
                .flatMap(user -> {
                    currentUser = user;
                    if(user.isLoggedIn()) {
                        if(isConnected) {
                            return vocabyApi.getWordData(user.getToken(), searched)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread());
                        }
                    }

                    // Get Local Definitions and Local Saves if above check fails
                    return vocabyRepository.getWordDataPackageLocally(searched, userId);
                }).subscribe(wordPackage -> {
                        mWordPackage.setValue(wordPackage);
                    }, error -> {
                        if(error instanceof EmptyResultSetException) {
                            mWordPackage.setValue(new WordDataPackage(new WordModel(searched), false));
                        }

                        Bugsnag.notify(error);
                        Log.e("retrieveWordDataFromRepo: ", error.getMessage());
                }
            )
        );
    }

    public void saveWord(String word, boolean isConnected) {
        if(mWordPackage.getValue() != null) {
            if(currentUser.isLoggedIn() && isConnected) {
                compositeDisposable.add(
                        vocabyRepository.getVocabyApiService("").save(currentUser.getToken(), word)
                                .andThen(vocabyRepository.addSave(new UserSaves(currentUser.getUserId(), word)))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> mWordPackage.setValue(mWordPackage.getValue().setSave(true)),
                                        error -> {
                                            if(error instanceof HttpException) {
                                                mWordPackage.setValue(mWordPackage.getValue().setSave(false));
                                            }

                                            Bugsnag.notify(error);
                                            Log.e("saveWord: ", error.getMessage());
                                        })
                );
            } else {
                if(currentUser.isLoggedIn()) {
                    remoteSaveSuccessful.setValue(false);
                }

                compositeDisposable.add(
                        vocabyRepository.addSave(new UserSaves(currentUser.getUserId(), word))
                                .subscribe(() -> mWordPackage.setValue(mWordPackage.getValue().setSave(true)),
                                        error -> {
                                            Bugsnag.notify(error);
                                            Log.e("saveWord (local, saved): ", error.getMessage());
                                        })
                );
            }
        }
    }

    public void removeSave(String word, Boolean isConnected) {
        if(mWordPackage.getValue() != null) {
            if (currentUser.isLoggedIn() && isConnected) {
                compositeDisposable.add(
                        vocabyRepository.getVocabyApiService("").removeSave(currentUser.getToken(), word)
                                .andThen(vocabyRepository.removeSave(currentUser.getUserId(), word))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> mWordPackage.setValue(mWordPackage.getValue().setSave(false)),
                                        error -> {
                                            if(error instanceof HttpException) {
                                                mWordPackage.setValue(mWordPackage.getValue().setSave(true));
                                            }

                                            Bugsnag.notify(error);
                                            Log.e("saveWord: ", error.getMessage());
                                        })
                );
            } else {
                if(currentUser.isLoggedIn()) {
                    remoteSaveSuccessful.setValue(false);
                }

                compositeDisposable.add(
                        vocabyRepository.removeSave(currentUser.getUserId(), word)
                                .subscribe(() -> mWordPackage.setValue(mWordPackage.getValue().setSave(false)),
                                        error -> {
                                            Bugsnag.notify(error);
                                            Log.e("saveWord (local): ", error.getMessage());
                                        })
                );
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
