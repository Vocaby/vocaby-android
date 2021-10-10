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
import com.vocaby.app.Constants;
import com.vocaby.app.data.entity.UserSaves;
import com.vocaby.app.models.EntryDataPackage;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SearchResultsViewModel extends AndroidViewModel {
    private final CompositeDisposable compositeDisposable;
    private final VocabyRepository vocabyRepository;
    private final SingleLiveEvent<EntryDataPackage> mEntryPackage;
    private final SingleLiveEvent<Boolean> mEntrySavedStatus;
    private final SharedPreferences userSharedPreference;

    public SearchResultsViewModel(@NonNull Application application) {
        super(application);
        userSharedPreference = application.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE);
        compositeDisposable = new CompositeDisposable();
        vocabyRepository = new VocabyRepository(application);
        mEntryPackage = new SingleLiveEvent<>();
        mEntrySavedStatus = new SingleLiveEvent<>();
    }

    public LiveData<EntryDataPackage> getWordData() {
        return mEntryPackage;
    }

    public LiveData<Boolean> getSavedStatus() {
        return mEntrySavedStatus;
    }

    public void retrieveWordDataFromRepo(String searched) {
        int userId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1);
        compositeDisposable.add(
                vocabyRepository.getUser(userId)
                        .flatMap(user -> {
                            // Get Local Definitions and Local Saves if above check fails
                            return vocabyRepository.getWordDataPackageLocally(searched, userId);
                        }).subscribe(mEntryPackage::setValue, error -> {
                            if (error instanceof EmptyResultSetException) {
                                mEntryPackage.setValue(new EntryDataPackage(new EntryModel(searched), new EntryModel(searched), false));
                            }

                            Bugsnag.notify(error);
                            Log.e("retrieveWordDataFromRepo: ", error.getMessage());
                        }
                )
        );
    }

    public void saveWord(String word) {
        if (mEntryPackage.getValue() != null) {
            int userId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1);
            compositeDisposable.add(
                    vocabyRepository.getUser(userId)
                            .flatMapCompletable(currentUser ->
                                    vocabyRepository.addSave(new UserSaves(currentUser.getUserId(), word))
                            ).subscribe(() -> mEntrySavedStatus.setValue(true),
                                    error -> {
                                        mEntrySavedStatus.setValue(false);

                                        Bugsnag.notify(error);
                                        Log.e("saveWord: ", error.getMessage());
                                    })
            );
        }
    }

    public void removeSave(String word) {
        if (mEntryPackage.getValue() != null) {
            int userId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1);
            compositeDisposable.add(
                    vocabyRepository.getUser(userId)
                            .flatMapCompletable(currentUser ->
                                    vocabyRepository.removeSave(currentUser.getUserId(), word)
                            ).subscribe(() -> mEntrySavedStatus.setValue(false),
                                    error -> {
                                        mEntrySavedStatus.setValue(true);

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
