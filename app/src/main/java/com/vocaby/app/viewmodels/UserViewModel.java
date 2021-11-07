package com.vocaby.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.models.payload.ItemStringPayload;
import com.vocaby.app.models.payload.PayloadState;
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
    private final SingleLiveEvent<ItemStringPayload> mItemChange;

    public UserViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();

        mSavedWords = new MutableLiveData<>(new ArrayList<>());
        mSaveCount = new MutableLiveData<>(0);
        mItemChange = new SingleLiveEvent<>();
    }

    private void addSaves() {
        compositeDisposable.add(
                vocabyRepository.getSortedDictionaryEntriesFromDB()
                        .flatMapCompletable(vocabyRepository::addSaves)
                        .subscribe(() -> {}, Throwable::printStackTrace)
        );
    }

    public void setupApplication() {
        compositeDisposable.add(
                vocabyRepository.checkUser()
                    .flatMap(result -> {
                        if (result == 1) {
                            return vocabyRepository.getUserSaves();
                        } else {
                            return vocabyRepository.createUserAndGetSaves();
                        }
                    }).subscribe(saves -> {
                        mSavedWords.setValue(saves);
                        mSaveCount.setValue(saves.size());
                }, Logger::reportErrorToBugsnag)
        );
    }

    public LiveData<List<String>> getSavedWords() {
        return mSavedWords;
    }
    public LiveData<Integer> getSaveCount() {
        return mSaveCount;
    }
    public LiveData<ItemStringPayload> getItemStatePayload() { return mItemChange; }

    public void addSaveItem(String entry) {
        if (mSavedWords.getValue() != null) {
            List<String> list = mSavedWords.getValue();
            ItemStringPayload itemStringPayload =
                    new ItemStringPayload(PayloadState.ADD, entry);
            mItemChange.setValue(itemStringPayload);

            list.add(0, entry);
            mSaveCount.setValue(list.size());
        }
    }

    public void removeSaveItem(String entry) {
        if (mSavedWords.getValue() != null) {
            List<String> list = mSavedWords.getValue();
            ItemStringPayload itemStringPayload =
                    new ItemStringPayload(PayloadState.DELETE, entry);
            mItemChange.setValue(itemStringPayload);

            list.remove(entry);
            mSaveCount.setValue(list.size());
        }
    }

    public void clearSaves() {
        compositeDisposable.add(
                vocabyRepository.clearSaves()
                        .subscribe(() -> {
                            mSavedWords.setValue(new ArrayList<>());
                            mSaveCount.setValue(0);
                        }, Logger::reportErrorToBugsnag)
        );
    }

    public void resetSaves() {
        compositeDisposable.add(
                vocabyRepository.getUserSaves()
                    .subscribe(saves -> {
                        mSavedWords.setValue(saves);
                        mSaveCount.setValue(saves.size());
                    }, Logger::reportErrorToBugsnag)
        );
    }

    public void setSavesCount() {
        if (mSavedWords.getValue() != null) {
            mSaveCount.setValue(mSavedWords.getValue().size());
        }

    }

    public void removeSaveFromDB(String entry) {
        compositeDisposable.add(
                vocabyRepository.removeSave(entry)
                        .subscribe(() -> removeSaveItem(entry), Logger::reportErrorToBugsnag)
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
