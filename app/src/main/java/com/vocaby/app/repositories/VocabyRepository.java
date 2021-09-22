package com.vocaby.app.repositories;

import android.app.Application;

import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.data.VocabyDatabase;
import com.vocaby.app.data.dao.VocabyDao;
import com.vocaby.app.data.entity.CustomDefinition;
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.data.entity.CustomEntryGroup;
import com.vocaby.app.data.entity.CustomExample;
import com.vocaby.app.data.entity.EntryWithDefinitions;
import com.vocaby.app.data.entity.OfflineAddedSaves;
import com.vocaby.app.data.entity.OfflineRemovedSaves;
import com.vocaby.app.data.entity.Type;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.data.entity.UserSaves;
import com.vocaby.app.data.entity.WordDefinitions;
import com.vocaby.app.models.OfflineDataModel;
import com.vocaby.app.models.WordDataPackage;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
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

    // Gets data from UI Thread
    public Single<List<String>> getDictionaryEntries() {
        return vocabyDao.getDictionaryEntries()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
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

    public Completable addUserSaves(List<UserSaves> saves){
        return vocabyDao.addSaves(saves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable removeUserSaves(List<String> saves){
        return vocabyDao.removeSaves(saves)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<String> getRandomUserSave(int id) {
        return vocabyDao.getRandomSave(id)
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

    public Single<Boolean> getUserSyncStatus(int userId) {
        return vocabyDao.getSyncStatus(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable setUserSyncStatus(boolean isSynced, int userId) {
        return vocabyDao.setUserSyncStatus(isSynced, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable deleteAllUsers(int localId) {
        return vocabyDao.removeAllUsers(localId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public VocabyApiService getVocabyApiService(int type) {
        return apiManager.getVocabyApiService(type);
    }

    // OFFLINE
    public Completable addOfflineAddedSave(String word) {
        return vocabyDao.addOfflineAddedSave(new OfflineAddedSaves(word))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable addOfflineDeletedSave(String word) {
        return vocabyDao.addOfflineRemovedSave(new OfflineRemovedSaves(word))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable removeOfflineAddedSave(String word) {
        return vocabyDao.removeOfflineAddedSave(word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable removeOfflineDeletedSave(String word) {
        return vocabyDao.removeOfflineRemovedSave(word)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<OfflineDataModel> getOfflineData() {
        return Single.zip(getOfflineAdded(), getOfflineRemoved(), OfflineDataModel::new);
    }

    public Completable clearOfflineDataInDatabase() {
        return vocabyDao.clearOfflineRemoved()
                .andThen(vocabyDao.clearOfflineAdded())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Single<List<String>> getOfflineAdded() {
        return vocabyDao.getOfflineAdded()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Single<List<String>> getOfflineRemoved() {
        return vocabyDao.getOfflineRemoved()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // CUSTOM USER SAVES
    public Completable deleteUserEntry(int userId, String entry) {
        return vocabyDao.deleteUserEntry(userId, entry)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Long> insertCustomEntry(int userId, String entry, long date) {
        return vocabyDao.insertCustomEntry(new CustomEntry(userId, entry, date))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Long>> insertCustomEntryGroups(List<CustomEntryGroup> groups) {
        return vocabyDao.insertCustomEntryGroups(groups)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Long>> insertCustomDefinitions(List<CustomDefinition> definitions) {
        return vocabyDao.insertCustomDefinitions(definitions)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Completable insertCustomExamples(List<CustomExample> examples) {
        return vocabyDao.insertCustomExamples(examples)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<EntryWithDefinitions> getEntryData(int entryId) {
        return vocabyDao.getUserEntryData(entryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<CustomEntry>> getUserEntries(int userId) {
        return vocabyDao.getUserEntries(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // Type mainthread
    public Completable insertTypes(List<Type> types) {
        return vocabyDao.insertTypes(types)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Type>> getTypes() {
        return vocabyDao.getTypes()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    // UTIL
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
