package com.vocaby.app.repositories;

import android.app.Application;

import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.database.VocabyDatabase;
import com.vocaby.app.database.dao.VocabyDao;
import com.vocaby.app.database.entity.OfflineAddedSaves;
import com.vocaby.app.database.entity.OfflineRemovedSaves;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.database.entity.WordDefinitions;
import com.vocaby.app.models.WordDataPackage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VocabyRepository {
    private VocabyDatabase vocabyDatabase;
    private ApiManager apiManager;
    private VocabyDao vocabyDao;


    public VocabyRepository(Application application) {
        vocabyDatabase = VocabyDatabase.getDatabase(application);
        vocabyDao = vocabyDatabase.vocabyDao();
        apiManager = ApiManager.getInstance();
    }

    public Single<WordDefinitions> getWordDataFromDatabase(String word) {
        return vocabyDao.getWordData(word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<WordDefinitions> getWordDataFromDatabase(int id) {
        return vocabyDao.getWordData(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Integer> hasSave(String word, int id) {
        return vocabyDao.hasSave(word, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<WordDataPackage> getWordDataPackageLocally(String word, int userId) {
        return Single.zip(
                getWordDataFromDatabase(word),
                hasSave(word, userId),
                WordDataPackage::new
        );
    }

    public Single<WordDefinitions> getRandomWord() {
        return vocabyDao.getRandomWord()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addSave(UserSaves userSaves) {
        return vocabyDao.addSave(userSaves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable removeSave(int id, String word) {
        return vocabyDao.removeSave(id, word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<User> getUser(int id) {
        return vocabyDao.getCurrentUser(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Long>> insertSavedWords(List<UserSaves> userSaves) {
        return vocabyDao.insertSavedWords(userSaves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> getUserSaves(int id) {
        return vocabyDao.getSaves(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<String> getRandomUserSave(int id) {
        return vocabyDao.getRandomSave(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addOfflineAddedSave(String word) {
        return vocabyDao.addOfflineAddedSave(new OfflineAddedSaves(word))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> getOfflineAddedSaves() {
        return vocabyDao.getOfflineAdded()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Completable addOfflineRemovedSave(String word) {
        return vocabyDao.addOfflineRemovedSave(new OfflineRemovedSaves(word))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<String>> getOfflineRemovedSaves() {
        return vocabyDao.getOfflineRemoved()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Map<String, List<String>>> getOfflineSaves() {
        return Single.zip(getOfflineAddedSaves(), getOfflineRemovedSaves(), (added, removed) -> {
            Map<String, List<String>> map = new HashMap<>();
            map.put("added", added);
            map.put("removed", removed);

            return map;
        });
    }

    public Completable clearOfflineData() {
        return vocabyDao.clearOfflineAdded()
                .andThen(vocabyDao.clearOfflineRemoved())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Long> createUser(User user) {
        return vocabyDao.createUser(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteUser(User user) {
        return vocabyDao.removeUser(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteAllUsers(int localId) {
        return vocabyDao.removeAllUsers(localId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public VocabyApiService getVocabyApiService(String type) {
        return apiManager.getVocabyApiService(type);
    }

    public Single<Integer> getSavesCount() {
        return vocabyDao.getSavesCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Integer> getUserCount() {
        return vocabyDao.getUserCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
