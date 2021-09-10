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
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.data.entity.UserSaves;
import com.vocaby.app.models.UserSavesSyncModel;
import com.vocaby.app.models.UserStateModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.NetworkManager;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class UserViewModel extends AndroidViewModel implements OnSaveItemButtonTouch {
    private final VocabyRepository vocabyRepository;
    private final MutableLiveData<User> mUser;
    private final MutableLiveData<List<String>> mSavedWords;
    private final SingleLiveEvent<Boolean> mSyncStatus;
    private final String CURRENT_ID_KEY = "CURRENT_USER_ID";
    private final MutableLiveData<UserStateModel> userState;
    private final CompositeDisposable compositeDisposable;
    SharedPreferences userSharedPreference;

    public UserViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        userSharedPreference = getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        userState = new MutableLiveData<>();
        mSavedWords = new MutableLiveData<>();
        mUser = new MutableLiveData<>();
        mSyncStatus = new SingleLiveEvent<>();
    }

    public void setupApplication() {
        int currentId = userSharedPreference.getInt(CURRENT_ID_KEY, 1);
        compositeDisposable.add(
                vocabyRepository.getUser(currentId)
                        .flatMap(user -> {
                            mUser.setValue(user);
                            return vocabyRepository.getUserSaves(currentId);
                        }).flatMap(saves -> {
                    mSavedWords.setValue(saves);
                    return vocabyRepository.getUserSyncStatus(currentId);
                }).subscribe(isSynced -> {
                    // User is Logged In. Check if offlineSaves exist
                    if (mUser.getValue() != null && mUser.getValue().isLoggedIn()) {
                        if (isSynced) {
                            userState.setValue(new UserStateModel(false, true));
                        } else {
                            userState.setValue(new UserStateModel(false, false));
                        }
                    } else {
                        // User is local
                        userState.setValue(new UserStateModel(true));
                    }
                }, e -> {
                    if (e instanceof EmptyResultSetException) {
                        addDefaultUser();
                    } else {
                        Bugsnag.notify(e);
                        Log.e("UserViewModel (current user): ", e.getMessage());
                    }
                })
        );
    }

    public void setUserState(UserStateModel userState) {
        this.userState.setValue(userState);
    }

    private void addDefaultUser() {
        User user = new User();
        compositeDisposable.add(
                vocabyRepository.createUser(user)
                        .subscribe(id -> {
                            int userId = id.intValue();
                            SharedPreferences.Editor editor = userSharedPreference.edit();
                            editor.putInt("LOCAL_USER_ID", userId);
                            editor.putInt("CURRENT_USER_ID", userId);
                            editor.apply();
                            mUser.setValue(user);
                            userState.setValue(new UserStateModel(true));
                        }, error -> {
                            Bugsnag.notify(error);
                            Log.e("UserViewModel (new user): ", error.getMessage());
                        })
        );
    }

    public LiveData<UserStateModel> getUserState() {
        return this.userState;
    }

    public LiveData<Boolean> getSyncStatus() {
        return this.mSyncStatus;
    }

    public void changeToCurrentUser() {
        compositeDisposable.add(
                vocabyRepository.getUser(userSharedPreference.getInt(CURRENT_ID_KEY, 1))
                        .subscribe(mUser::setValue, Throwable::printStackTrace)
        );
    }

    public void handleActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            // User Logged In. Account should always be synced at this point.
            if (result.getData() != null && result.getData().getBooleanExtra("loginStatus", false)) {
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

    private void setSavedWordsFromDatabase() {
        compositeDisposable.add(
                vocabyRepository.getUserSaves(userSharedPreference.getInt(CURRENT_ID_KEY, 1))
                        .subscribe(mSavedWords::setValue, error -> {
                            Bugsnag.notify(error);
                            Log.e("UserViewModel (setSavedWordsFromDatabase): ", error.getMessage());
                        })
        );
    }

    public String getSaveItem(int position) {
        if (mSavedWords.getValue() != null) {
            return mSavedWords.getValue().get(position);
        }

        return null;
    }

    public void logout() {
        int localId = userSharedPreference.getInt("LOCAL_USER_ID", 1);
        Completable deleteAllUsers = vocabyRepository.deleteAllUsers(localId);
        Single<User> getLocalUser = vocabyRepository.getUser(localId);
        if (mUser.getValue() != null) {
            String token = mUser.getValue().getToken();
            compositeDisposable.add(
                    deleteAllUsers
                            .andThen(getLocalUser)
                            .flatMapCompletable(user -> {
                                mUser.setValue(user);

                                // Application should always be in the local state once the user logs out.
                                userState.setValue(new UserStateModel(true));
                                // Set current user ID back to local user ID
                                SharedPreferences.Editor editor = userSharedPreference.edit();
                                editor.putInt(CURRENT_ID_KEY, user.getUserId());
                                editor.apply();

                                return vocabyRepository.getVocabyApiService(VocabyApiService.DEFAULT).logout(token)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread());
                            }).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                            }, error -> {
                                if (!(error instanceof HttpException)) {
                                    Log.e("logout: ", error.getMessage());
                                    Bugsnag.notify(error);
                                }
                            })
            );
        }
    }

    public void syncUserSaves() {
        // Get Offline Data and send it to the API
        User currentUser = mUser.getValue();
        UserStateModel userState = this.userState.getValue();
        if (currentUser != null && userState != null) {
            if (currentUser.isLoggedIn()) {
                mSyncStatus.setValue(false);
                syncFromRemote(currentUser, userState.isSynced());
                setSavedWordsFromDatabase();
            } else {
                setSavedWordsFromDatabase();
            }
        }
    }

    private void syncFromRemote(User currentUser, boolean isSynced) {
        compositeDisposable.add(
                vocabyRepository.getVocabyApiService(VocabyApiService.DEFAULT).getSaves(currentUser.getToken())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMapCompletable(remoteSaves -> {
                            // Look for words that were added in remote
                            if (isSynced) {
                                UserSavesSyncModel wordsToUpdateLocally = getWordsToUpdateLocally(
                                        remoteSaves,
                                        null,
                                        null,
                                        currentUser.getUserId()
                                );


                                return vocabyRepository.addUserSaves(wordsToUpdateLocally.getWordsToAddLocally())
                                        .andThen(vocabyRepository.removeUserSaves(wordsToUpdateLocally.getWordsToRemoveLocally()));
                            } else {
                                return vocabyRepository.getOfflineData()
                                        .flatMapCompletable(offlineData -> {
                                            UserSavesSyncModel wordsToUpdateLocally = getWordsToUpdateLocally(
                                                    remoteSaves,
                                                    offlineData.getOfflineRemoved(),
                                                    offlineData.getOfflineAdded(),
                                                    currentUser.getUserId()
                                            );

                                            return vocabyRepository.getVocabyApiService(VocabyApiService.DEFAULT).syncData(currentUser.getToken(), offlineData)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .andThen(vocabyRepository.addUserSaves(wordsToUpdateLocally.getWordsToAddLocally()))
                                                    .andThen(vocabyRepository.removeUserSaves(wordsToUpdateLocally.getWordsToRemoveLocally()))
                                                    .andThen(vocabyRepository.clearOfflineDataInDatabase())
                                                    .andThen(vocabyRepository.setUserSyncStatus(true, currentUser.getUserId()));
                                        });
                            }
                        }).andThen(vocabyRepository.setUserSyncStatus(true, currentUser.getUserId()))
                        .subscribe(() -> {
                            userState.setValue(new UserStateModel(false, true));
                            SharedPreferences.Editor offlineEditor =
                                    getApplication().getSharedPreferences("OFFLINE", Context.MODE_PRIVATE).edit();
                            offlineEditor.clear();
                            offlineEditor.apply();
                            mSyncStatus.setValue(true);
                        }, error -> {
                            mSyncStatus.setValue(true);
                            userState.setValue(new UserStateModel(false, false));
                            Bugsnag.notify(error);
                        })
        );
    }

    private UserSavesSyncModel getWordsToUpdateLocally(List<String> remoteSaves,
                                                       List<String> addBlacklist,
                                                       List<String> removeBlackList, int userId) {

        List<String> currentSaves = mSavedWords.getValue();
        List<UserSaves> wordsToAddLocally = new ArrayList<>();
        List<String> wordsToRemoveLocally = new ArrayList<>();

        if (currentSaves != null) {
            HashSet<String> whitelistSet = new HashSet<>(currentSaves);
            if (addBlacklist != null) {
                whitelistSet.addAll(addBlacklist);
            }

            for (String word : remoteSaves) {
                if (!whitelistSet.contains(word)) {
                    wordsToAddLocally.add(new UserSaves(userId, word));
                    currentSaves.add(word);
                }
            }

            whitelistSet = new HashSet<>(remoteSaves);
            if (removeBlackList != null) {
                whitelistSet.addAll(removeBlackList);
            }

            for (Iterator<String> it = currentSaves.iterator(); it.hasNext(); ) {
                String wordToRemove = it.next();
                if (!whitelistSet.contains(wordToRemove)) {
                    wordsToRemoveLocally.add(wordToRemove);
                    it.remove();
                }
            }
        }

        mSavedWords.setValue(currentSaves);
        return new UserSavesSyncModel(wordsToAddLocally, wordsToRemoveLocally);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    @Override
    public void removeSave(String word) {
        int userId = userSharedPreference.getInt("CURRENT_USER_ID", 1);
        compositeDisposable.add(
                vocabyRepository.getUser(userId)
                        .flatMapCompletable(currentUser -> {
                            if (currentUser.isLoggedIn()) {
                                if (NetworkManager.isConnectedToInternet(getApplication())) {
                                    // Remove in remote save and then remove in local save
                                    return vocabyRepository.getVocabyApiService(VocabyApiService.DEFAULT).removeSave(currentUser.getToken(), word)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .andThen(vocabyRepository.removeSave(currentUser.getUserId(), word));
                                } else {
                                    setUserState(new UserStateModel(false, false));
                                    Completable setSync = vocabyRepository.setUserSyncStatus(false, currentUser.getUserId());
                                    if (!currentUser.isSynced()) {
                                        setSync = Completable.complete();
                                    }

                                    SharedPreferences offlineSharedPreferences =
                                            getApplication().getSharedPreferences("OFFLINE", Context.MODE_PRIVATE);

                                    if (offlineSharedPreferences.contains("#!new-" + word)) {
                                        return vocabyRepository.removeOfflineAddedSave(word)
                                                .andThen(vocabyRepository.removeSave(currentUser.getUserId(), word))
                                                .andThen(setSync);
                                    } else {
                                        SharedPreferences.Editor offlineEditor = offlineSharedPreferences.edit();
                                        offlineEditor.putString("#!original-" + word, word);
                                        offlineEditor.apply();

                                        return vocabyRepository.addOfflineDeletedSave(word)
                                                .andThen(vocabyRepository.removeSave(currentUser.getUserId(), word))
                                                .andThen(setSync);
                                    }
                                }
                            } else {
                                return vocabyRepository.removeSave(currentUser.getUserId(), word);
                            }
                        }).subscribe(() -> {}, Bugsnag::notify)
        );
    }
}
