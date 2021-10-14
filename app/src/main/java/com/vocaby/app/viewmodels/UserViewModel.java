package com.vocaby.app.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.data.entity.User;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class UserViewModel extends AndroidViewModel {
    private static final int ADD_SAVE = 1;
    private static final int REMOVE_SAVE = 2;

    private final VocabyRepository vocabyRepository;
    private final MutableLiveData<List<String>> mSavedWords;
    private final MutableLiveData<Integer> mSaveCount;
    private final MutableLiveData<User> mUser;
    private final CompositeDisposable compositeDisposable;

    public UserViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        mSavedWords = new MutableLiveData<>(new ArrayList<>());
        compositeDisposable = new CompositeDisposable();
        mSaveCount = new MutableLiveData<>(0);
        mUser = new MutableLiveData<>();
    }

    public void setupApplication() {
        compositeDisposable.add(
                vocabyRepository.getCurrentUserId()
                .flatMap(vocabyRepository::getUser)
                .flatMap(user -> {
                    mUser.setValue(user);
                    return vocabyRepository.getUserSaves(user.getUserId());
                }).subscribe(saves -> {
                    mSavedWords.setValue(saves);
                    mSaveCount.setValue(saves.size());
                }, e -> {
                    if (e instanceof EmptyResultSetException) {
                        addDefaultUser();
                    } else {
                        Logger.reportError(e);
                        Log.e("UserViewModel (current user): ", e.getMessage());
                    }
                })
        );
    }

    private void addDefaultUser() {
        User user = new User();
        compositeDisposable.add(
                vocabyRepository.createUser(user)
                .flatMapCompletable(id -> {
                    int userId = id.intValue();
                    user.setUserId(userId);
                    mUser.setValue(user);
                    return vocabyRepository.writeUserId(userId);
                }).subscribe(() -> {}, error -> {
                    Logger.reportError(error);
                    Log.e("UserViewModel (new user): ", error.getMessage());
                })
        );
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<List<String>> getSavedWords() {
        return mSavedWords;
    }

    public LiveData<Integer> getSaveCount() {
        return mSaveCount;
    }

    public String getSaveItem(int position) {
        if (mSavedWords.getValue() != null) {
            return mSavedWords.getValue().get(position);
        }

        return null;
    }

    public void addSaveItem(String entry) {
        if (mSavedWords.getValue() != null) {
            List<String> list = mSavedWords.getValue();
            list.add(0, entry);
            mSavedWords.setValue(list);
            mSaveCount.setValue(list.size());
        }
    }

    public void removeSaveItem(String entry) {
        if (mSavedWords.getValue() != null) {
            List<String> list = mSavedWords.getValue();
            list.remove(entry);
            mSavedWords.setValue(list);
            mSaveCount.setValue(list.size());
        }
    }

    public void setSavesCount() {
        if (mSavedWords.getValue() != null) {
            mSaveCount.setValue(mSavedWords.getValue().size());
        }

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public void removeSave(String word) {
        compositeDisposable.add(
                vocabyRepository.getCurrentUserId()
                    .flatMap(vocabyRepository::getUser)
                    .flatMapCompletable(currentUser -> {
                            removeSaveItem(word);
                            return vocabyRepository.removeSave(currentUser.getUserId(), word);
                        }
                    ).subscribe(() -> {
                    }, Logger::reportError)
        );
    }

}
