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
import com.vocaby.app.data.entity.UserSaves;
import com.vocaby.app.models.WordDataPackage;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class SearchResultsViewModel extends AndroidViewModel {
    private final CompositeDisposable compositeDisposable;
    private final VocabyRepository vocabyRepository;
    private final SingleLiveEvent<WordDataPackage> mWordPackage;
    private final SharedPreferences userSharedPreference;
    private final SharedPreferences offlineSharedPreferences;
    private final SingleLiveEvent<Boolean> remoteSaveSuccessful;

    public SearchResultsViewModel(@NonNull Application application) {
        super(application);
        userSharedPreference = application.getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        offlineSharedPreferences = application.getSharedPreferences("OFFLINE", Context.MODE_PRIVATE);
        compositeDisposable = new CompositeDisposable();
        vocabyRepository = new VocabyRepository(application);
        mWordPackage = new SingleLiveEvent<>();
        remoteSaveSuccessful = new SingleLiveEvent<>();
    }

    public LiveData<WordDataPackage> getWordData() {
        return mWordPackage;
    }

    public LiveData<Boolean> getRemoteSaveStatus() {
        return remoteSaveSuccessful;
    }

    public void retrieveWordDataFromRepo(String searched, Boolean isConnected) {
        int userId = userSharedPreference.getInt("CURRENT_USER_ID", 1);
        VocabyApiService vocabyApi = vocabyRepository.getVocabyApiService(VocabyApiService.DEFINITION);

        compositeDisposable.add(
                vocabyRepository.getUser(userId)
                        .flatMap(user -> {
                            if (user.isLoggedIn()) {
                                if (isConnected) {
                                    return vocabyApi.getWordData(user.getToken(), searched)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread());
                                }
                            }
                            // Get Local Definitions and Local Saves if above check fails
                            return vocabyRepository.getWordDataPackageLocally(searched, userId);
                        }).subscribe(mWordPackage::setValue, error -> {
                            if (error instanceof EmptyResultSetException) {
                                mWordPackage.setValue(new WordDataPackage(new EntryModel(searched), false));
                            }

                            Bugsnag.notify(error);
                            Log.e("retrieveWordDataFromRepo: ", error.getMessage());
                        }
                )
        );
    }

    public void saveWord(String word, boolean isConnected) {
        if (mWordPackage.getValue() != null) {
            int userId = userSharedPreference.getInt("CURRENT_USER_ID", 1);
            compositeDisposable.add(
                    vocabyRepository.getUser(userId)
                            .flatMapCompletable(currentUser -> {
                                if (currentUser.isLoggedIn()) {
                                    if (isConnected) {
                                        // Add to remote save and then add to local save
                                        return vocabyRepository.getVocabyApiService(VocabyApiService.DEFAULT).save(currentUser.getToken(), word)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .andThen(vocabyRepository.addSave(new UserSaves(currentUser.getUserId(), word)));
                                    } else {
                                        // Offline Added Save.
                                        remoteSaveSuccessful.setValue(false);
                                        Completable setSync = vocabyRepository.setUserSyncStatus(false, currentUser.getUserId());
                                        if(!currentUser.isSynced()) {
                                            setSync = Completable.complete();
                                        }

                                        if (offlineSharedPreferences.contains("#!original-" + word)) {
                                            // The word was saved before the user went offline.
                                            return vocabyRepository.removeOfflineDeletedSave(word)
                                                    .andThen(vocabyRepository.addSave(new UserSaves(currentUser.getUserId(), word)))
                                                    .andThen(setSync);
                                        } else {
                                            SharedPreferences.Editor offlineEditor = offlineSharedPreferences.edit();
                                            offlineEditor.putString("#!new-" + word, word);
                                            offlineEditor.apply();

                                            return vocabyRepository.addOfflineAddedSave(word)
                                                    .andThen(vocabyRepository.addSave(new UserSaves(currentUser.getUserId(), word)))
                                                    .andThen(setSync);
                                        }
                                    }
                                } else {
                                    // User is local
                                    return vocabyRepository.addSave(new UserSaves(currentUser.getUserId(), word));
                                }
                            }).subscribe(() -> mWordPackage.setValue(mWordPackage.getValue().setSave(true)),
                                    error -> {
                                        if (error instanceof HttpException) {
                                            mWordPackage.setValue(mWordPackage.getValue().setSave(false));
                                        }

                                        Bugsnag.notify(error);
                                        Log.e("saveWord: ", error.getMessage());
                                    })
            );
        }
    }

    public void removeSave(String word, Boolean isConnected) {
        if (mWordPackage.getValue() != null) {
            int userId = userSharedPreference.getInt("CURRENT_USER_ID", 1);
            compositeDisposable.add(
                    vocabyRepository.getUser(userId)
                            .flatMapCompletable(currentUser -> {
                                if (currentUser.isLoggedIn()) {
                                    if (isConnected) {
                                        // Remove in remote save and then remove in local save
                                        return vocabyRepository.getVocabyApiService(VocabyApiService.DEFAULT).removeSave(currentUser.getToken(), word)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .andThen(vocabyRepository.removeSave(currentUser.getUserId(), word));
                                    } else {
                                        // Offline Removed Save
                                        remoteSaveSuccessful.setValue(false);

                                        Completable setSync = vocabyRepository.setUserSyncStatus(false, currentUser.getUserId());
                                        if(!currentUser.isSynced()) {
                                            setSync = Completable.complete();
                                        }

                                        if (offlineSharedPreferences.contains("#!new-" + word)) {
                                            return vocabyRepository.removeOfflineAddedSave(word)
                                                    .andThen(vocabyRepository.removeSave(currentUser.getUserId(), word))
                                                    .andThen(setSync);
                                        } else {
                                            // Words that existed before the user went offline need
                                            // to be removed first.
                                            SharedPreferences.Editor offlineEditor = offlineSharedPreferences.edit();
                                            offlineEditor.putString("#!original-" + word, word);
                                            offlineEditor.apply();

                                            return vocabyRepository.addOfflineDeletedSave(word)
                                                    .andThen(vocabyRepository.removeSave(currentUser.getUserId(), word))
                                                    .andThen(setSync);
                                        }
                                    }
                                } else {
                                    // User is local
                                    return vocabyRepository.removeSave(currentUser.getUserId(), word);
                                }
                            }).subscribe(() -> mWordPackage.setValue(mWordPackage.getValue().setSave(false)),
                                    error -> {
                                        if (error instanceof HttpException) {
                                            mWordPackage.setValue(mWordPackage.getValue().setSave(true));
                                        }

                                        Bugsnag.notify(error);
                                        Log.e("saveWord: ", error.getMessage());
                                    }
                            )
            );
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
