package com.vocaby.app.repositories;

import android.app.Application;
import android.util.Log;

import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.database.DatabaseManager;
import com.vocaby.app.database.VocabyDatabase;
import com.vocaby.app.database.dao.VocabyDao;
import com.vocaby.app.database.entity.Definition;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.database.entity.Word;
import com.vocaby.app.database.entity.WordDefinitions;
import com.vocaby.app.models.WordModel;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VocabyRepository {
    private DatabaseManager databaseManager;
    private ApiManager apiManager;
    private VocabyDao vocabyDao;


    public VocabyRepository(Application application) {
        VocabyDatabase vocabyDatabase = VocabyDatabase.getDatabase(application);
        vocabyDao = vocabyDatabase.vocabyDao();
        databaseManager = DatabaseManager.getInstance(application);
        apiManager = ApiManager.getInstance();
    }

    public Completable clearUser() {
        return vocabyDao.clearUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Integer> getUserCount() {
        return vocabyDao.getUserCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<WordDefinitions> getWordDataFromDatabase(String word) {
        return vocabyDao.getWordData(word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<User> getCurrentUser(int id) {
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

    public Single<Long> createUser(User user) {
        return vocabyDao.createUser(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public VocabyApiService getVocabyApiService(String type) {
        return apiManager.getVocabyApiService(type);
    }
}
