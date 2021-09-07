package com.vocaby.app.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vocaby.app.database.entity.OfflineAddedSaves;
import com.vocaby.app.database.entity.OfflineRemovedSaves;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.database.entity.Word;
import com.vocaby.app.database.entity.WordDefinitions;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public abstract class VocabyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract Single<Long> createUser(User user);

    @Delete
    public abstract Completable removeUser(User user);

    @Query("DELETE FROM vocaby_user WHERE user_id != :id")
    public abstract Completable removeAllUsers(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract Completable addSave(UserSaves userSaves);

    @Query("DELETE FROM saves WHERE user_id = :id AND word = :word")
    public abstract Completable removeSave(int id, String word);

    @Query("SELECT EXISTS(SELECT 1 FROM saves WHERE word = :word AND user_id = :id)")
    public abstract Single<Integer> hasSave(String word, int id);

    @Query("DELETE FROM saves WHERE user_id = :id")
    public abstract Completable removeAllSaves(int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract Completable addSaves(List<UserSaves> userSaves);

    @Query("DELETE FROM saves WHERE word IN (:wordList)")
    public abstract Completable removeSaves(List<String> wordList);

    @Insert
    public abstract Single<List<Long>> insertSavedWords(List<UserSaves> userSaves);

    @Query("SELECT word FROM saves where user_id = :id")
    public abstract Single<List<String>> getSaves(int id);

    @Query("SELECT word FROM saves where user_id = :id ORDER BY RANDOM() LIMIT 1")
    public abstract Single<String> getRandomSave(int id);

    @Query("DELETE FROM saves")
    public abstract Completable clearSaves();

    @Transaction
    @Query("SELECT COUNT(*) FROM vocaby_user")
    public abstract Single<Integer> getUserCount();

    @Query("SELECT COUNT(*) FROM saves")
    public abstract Single<Integer> getSavesCount();

    @Transaction
    @Query("SELECT * FROM vocaby_user WHERE user_id = :id")
    public abstract Single<User> getCurrentUser(int id);

    @Query("SELECT synced FROM vocaby_user WHERE user_id = :id")
    public abstract Single<Boolean> getSyncStatus(int id);

    @Query("UPDATE vocaby_user SET synced = :synced WHERE user_id = :id")
    public abstract Completable setUserSyncStatus(boolean synced, int id);

    @Transaction
    @Query("SELECT * FROM dictionary_word WHERE word = :word")
    public abstract Single<WordDefinitions> getWordData(String word);

    @Transaction
    @Query("SELECT * FROM dictionary_word WHERE id = :id")
    public abstract Single<WordDefinitions> getWordData(int id);

    @Transaction
    @Query("SELECT * FROM dictionary_word WHERE id = " +
            "(SELECT id FROM dictionary_word ORDER BY RANDOM() LIMIT 1)")
    public abstract Single<WordDefinitions> getRandomWord();

    // OFFLINE SAVES
    @Insert
    public abstract Completable addOfflineAddedSave(OfflineAddedSaves offlineAddedSaves);

    @Insert
    public abstract Completable addOfflineRemovedSave(OfflineRemovedSaves offlineRemovedSaves);

    @Query("DELETE FROM offline_added WHERE word = :word")
    public abstract Completable removeOfflineAddedSave(String word);

    @Query("DELETE FROM offline_removed WHERE word = :word")
    public abstract Completable removeOfflineRemovedSave(String word);

    @Query("SELECT word FROM offline_added")
    public abstract Single<List<String>> getOfflineAdded();

    @Query("SELECT word FROM offline_removed")
    public abstract Single<List<String>> getOfflineRemoved();

    @Query("DELETE FROM offline_added")
    public abstract Completable clearOfflineAdded();

    @Query("DELETE FROM offline_removed")
    public abstract Completable clearOfflineRemoved();
}
