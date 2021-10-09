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

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.Constants;
import com.vocaby.app.adapters.OnSaveItemButtonTouch;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class UserViewModel extends AndroidViewModel implements OnSaveItemButtonTouch {
    private static final int ADD_SAVE = 1;
    private static final int REMOVE_SAVE = 2;

    private final VocabyRepository vocabyRepository;
    private final MutableLiveData<List<String>> mSavedWords;
    private final MutableLiveData<User> mUser;
    private final CompositeDisposable compositeDisposable;
    SharedPreferences userSharedPreference;

    public UserViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        mSavedWords = new MutableLiveData<>(new ArrayList<>());
        compositeDisposable = new CompositeDisposable();
        userSharedPreference = getApplication().getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE);
        mUser = new MutableLiveData<>();
    }

    public void setupApplication() {
        int currentId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1);
        compositeDisposable.add(
                vocabyRepository.getUser(currentId)
                        .flatMap(user -> {
                            mUser.setValue(user);
                            return vocabyRepository.getUserSaves(user.getUserId());
                        }).subscribe(mSavedWords::setValue, e -> {
                            if (e instanceof EmptyResultSetException) {
                                addDefaultUser();
                            } else {
                                Bugsnag.notify(e);
                                Log.e("UserViewModel (current user): ", e.getMessage());
                            }
                })
        );
    }

    private void addDefaultUser() {
        User user = new User();
        compositeDisposable.add(
                vocabyRepository.createUser(user)
                        .subscribe(id -> {
                            int userId = id.intValue();
                            SharedPreferences.Editor editor = userSharedPreference.edit();
                            editor.putInt("LOCAL_USER_ID", userId);
                            editor.putInt(Constants.CURRENT_USER_ID_KEY, userId);
                            editor.apply();
                            mUser.setValue(user);
                        }, error -> {
                            Bugsnag.notify(error);
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
        }
    }

    public void removeSaveItem(String entry) {
        if (mSavedWords.getValue() != null) {
            List<String> list = mSavedWords.getValue();
            list.remove(entry);
            mSavedWords.setValue(list);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    @Override
    public void removeSave(String word) {
        int userId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1);
        compositeDisposable.add(
                vocabyRepository.getUser(userId)
                        .flatMapCompletable(currentUser ->
                                vocabyRepository.removeSave(currentUser.getUserId(), word))
                        .subscribe(() -> {
                        }, Bugsnag::notify)
        );
    }
}
