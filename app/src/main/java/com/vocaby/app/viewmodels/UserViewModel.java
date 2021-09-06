package com.vocaby.app.viewmodels;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.adapters.OnSaveItemButtonTouch;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.models.UserStateModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.NetworkManager;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class UserViewModel extends AndroidViewModel implements OnSaveItemButtonTouch {
    private final VocabyRepository vocabyRepository;
    private final MutableLiveData<User> mUser;
    private final MutableLiveData<List<String>> mSavedWords;
    private final String CURRENT_ID_KEY = "CURRENT_USER_ID";
    private final MutableLiveData<UserStateModel> userState;
    private final CompositeDisposable compositeDisposable;
    SharedPreferences sharedPreferences;

    public UserViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        sharedPreferences = getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        userState = new MutableLiveData<>();
        mSavedWords = new MutableLiveData<>();
        mUser = new MutableLiveData<>();
    }

    public void setupApplication() {
        int currentId = sharedPreferences.getInt(CURRENT_ID_KEY, 1);
        compositeDisposable.add(
                vocabyRepository.getUser(currentId)
                        .flatMap(user -> {
                            mUser.setValue(user);
                            return vocabyRepository.getUserSaves(currentId);
                        }).flatMap(saves -> {
                            mSavedWords.setValue(saves);
                            return vocabyRepository.getOfflineSavesCount();
                        }).subscribe(num -> {
                            // User is Logged In. Check if offlineSaves exist
                            if(mUser.getValue() != null && mUser.getValue().isLoggedIn()) {
                                if(num > 0) {
                                    userState.setValue(new UserStateModel(false, false));
                                } else {
                                    userState.setValue(new UserStateModel(false, true));
                                }
                            } else {
                                // User is local
                                userState.setValue(new UserStateModel(true));
                            }
                        }, e -> {
                            if(e instanceof EmptyResultSetException) {
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
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("LOCAL_USER_ID", userId);
                            editor.putInt("CURRENT_USER_ID", userId);
                            editor.apply();
                            mUser.setValue(user);
                            userState.setValue(new UserStateModel(true));
                            setSavedWords();
                        }, error -> {
                            Bugsnag.notify(error);
                            Log.e("UserViewModel (new user): ", error.getMessage());
                        })
        );
    }

    public LiveData<UserStateModel> getUserState() {
        return this.userState;
    }

    public void refreshState() {
        compositeDisposable.add(
                vocabyRepository.getOfflineSavesCount()
                    .subscribe(num -> {
                        if(mUser.getValue() != null && mUser.getValue().isLoggedIn()) {
                            if(num > 0) {
                                userState.setValue(new UserStateModel(false, false));
                            } else {
                                userState.setValue(new UserStateModel(false, true));
                            }
                        } else {
                            userState.setValue(new UserStateModel(true));
                        }
                    })
        );
    }

    public void changeToCurrentUser() {
        compositeDisposable.add(
            vocabyRepository.getUser(sharedPreferences.getInt(CURRENT_ID_KEY, 1))
                .subscribe(user -> {
                        mUser.setValue(user);
                        setSavedWords();
                    },
                        Throwable::printStackTrace)
        );
    }

    public void handleActivityResult(ActivityResult result) {
        if(result.getResultCode() == Activity.RESULT_OK) {
            // User Logged In. Account should always be synced at this point.
            if(result.getData() != null && result.getData().getBooleanExtra("loginStatus", false)) {
                changeToCurrentUser();
                userState.setValue(new UserStateModel(false, true));
            }
        }
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public LiveData<List<String>> getSavedWords() {
        return mSavedWords;
    }

    public void setSavedWords() {
        compositeDisposable.add(
            vocabyRepository.getUserSaves(sharedPreferences.getInt(CURRENT_ID_KEY, 1))
                .subscribe(mSavedWords::setValue, error -> {
                    Bugsnag.notify(error);
                    Log.e("UserViewModel (setSavedWords): ", error.getMessage());
                })
        );
    }

    public String getSaveItem(int position) {
        if(mSavedWords.getValue() != null) {
            return mSavedWords.getValue().get(position);
        }

        return null;
    }

    public void logout() {
        int localId = sharedPreferences.getInt("LOCAL_USER_ID", 1);
        Completable deleteAllUsers = vocabyRepository.deleteAllUsers(localId);
        Completable clearOfflineData = vocabyRepository.clearOfflineData();
        Single<User> getLocalUser = vocabyRepository.getUser(localId);
        if(mUser.getValue() != null) {
            String token = mUser.getValue().getToken();
            compositeDisposable.add(
                deleteAllUsers
                    .andThen(clearOfflineData)
                    .andThen(getLocalUser)
                    .flatMapCompletable(user -> {
                        mUser.setValue(user);

                        // Application should always be in the local state once the user logs out.
                        userState.setValue(new UserStateModel(true));
                        // Set Saves to Local data
                        setSavedWords();
                        // Set current user ID back to local user ID
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(CURRENT_ID_KEY, user.getUserId());
                        editor.apply();

                        return vocabyRepository.getVocabyApiService("").logout(token)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());
                    }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {}, error -> {
                            if(!(error instanceof HttpException)) {
                                Log.e("logout: ", error.getMessage());
                                Bugsnag.notify(error);
                            }
                    })
        );
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    @Override
    public void removeSave(String word) {
        User currentUser = mUser.getValue();
        if(currentUser != null) {
            if (currentUser.isLoggedIn()) {
                if(NetworkManager.isConnectedToInternet(getApplication())) {
                    compositeDisposable.add(
                            vocabyRepository.getVocabyApiService("").removeSave(currentUser.getToken(), word)
                                    .andThen(vocabyRepository.removeSave(currentUser.getUserId(), word))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {},
                                            error -> {
                                                Bugsnag.notify(error);
                                                Log.e("removeWordFromSaves(api): ", error.getMessage());
                                            })
                    );
                } else {
                    // Add to Offline Save
                    // AndThen User Save
                    // Set sync to false (Just check this onResume?)
                }
            } else {
                // local remove
                compositeDisposable.add(
                        vocabyRepository.removeSave(mUser.getValue().getUserId(), word)
                                .subscribe(() -> {},
                                    error -> {
                                        Bugsnag.notify(error);
                                        Log.e("removeWordFromSaves(local): ", error.getMessage());
                                    })
                );

            }
        }
    }
}
