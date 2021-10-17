package com.vocaby.app.viewmodels;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.data.entity.User;
import com.vocaby.app.models.ItemStatePayload;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class UserViewModel extends AndroidViewModel {
    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private final MutableLiveData<List<String>> mSavedWords;
    private final MutableLiveData<Integer> mSaveCount;
    private final MutableLiveData<User> mUser;
    private final SingleLiveEvent<ItemStatePayload<String>> mItemChange;
    private final SingleLiveEvent<Integer> mEmptyCardVisibility;

    public UserViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();

        mSavedWords = new MutableLiveData<>(new ArrayList<>());
        mSaveCount = new MutableLiveData<>(0);
        mUser = new MutableLiveData<>();
        mItemChange = new SingleLiveEvent<>();
        mEmptyCardVisibility = new SingleLiveEvent<>();
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

                    if (saves.size() > 0) {
                        mEmptyCardVisibility.setValue(View.GONE);
                    } else {
                        mEmptyCardVisibility.setValue(View.VISIBLE);
                    }
                }, e -> {
                    if (e instanceof EmptyResultSetException) {
                        addDefaultUser();
                    } else {
                        Logger.reportError(e);
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
                }).subscribe(() -> {}, Logger::reportError)
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
    public LiveData<ItemStatePayload<String>> getItemStatePayload() { return mItemChange; }
    public LiveData<Integer> getEmptyCardVisibility() { return mEmptyCardVisibility; }

    public void addSaveItem(String entry) {
        if (mSavedWords.getValue() != null) {
            List<String> list = mSavedWords.getValue();
            ItemStatePayload<String> itemStatePayload =
                    new ItemStatePayload<>(ItemStatePayload.ADD, entry);
            mItemChange.setValue(itemStatePayload);

            list.add(0, entry);
            mSaveCount.setValue(list.size());
            if (list.size() == 1) mEmptyCardVisibility.setValue(View.GONE);
        }
    }

    public void removeSaveItem(String entry) {
        if (mSavedWords.getValue() != null) {
            List<String> list = mSavedWords.getValue();
            ItemStatePayload<String> itemStatePayload =
                    new ItemStatePayload<>(ItemStatePayload.DELETE, entry);
            mItemChange.setValue(itemStatePayload);

            list.remove(entry);
            mSaveCount.setValue(list.size());
            if (list.size() == 0) mEmptyCardVisibility.setValue(View.VISIBLE);
        }
    }

    public void setSavesCount() {
        if (mSavedWords.getValue() != null) {
            mSaveCount.setValue(mSavedWords.getValue().size());
        }

    }

    public void removeSaveFromDB(String entry) {
        compositeDisposable.add(
                vocabyRepository.getCurrentUserId()
                    .flatMap(vocabyRepository::getUser)
                    .flatMapCompletable(currentUser ->
                            vocabyRepository.removeSave(currentUser.getUserId(), entry)
                    ).subscribe(() -> removeSaveItem(entry), Logger::reportError)
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
