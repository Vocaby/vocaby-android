package com.vocaby.app.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vocaby.app.database.entity.User;
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.database.entity.Word;
import com.vocaby.app.database.entity.WordDefinitions;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public abstract class VocabyDao {
    @Insert
    public abstract Single<Long> createUser(User user);

    @Insert
    public abstract Completable addAllSaves(Word... words);

    @Delete
    public abstract Completable removeSave(Word word);

    @Query("DELETE FROM vocaby_user")
    public abstract Completable clearUser();

    @Insert
    public abstract Single<List<Long>> insertSavedWords(List<UserSaves> userSaves);

    @Insert
    public abstract Completable addSave(UserSaves userSaves);

    @Query("SELECT word FROM saves where userId = :id")
    public abstract Single<List<String>> getSaves(int id);

    @Query("DELETE FROM saves")
    public abstract Completable clearSaves();

    @Transaction
    @Query("SELECT COUNT(*) FROM vocaby_user")
    public abstract Single<Integer> getUserCount();

    @Transaction
    @Query("SELECT * FROM vocaby_user WHERE user_id = :id")
    public abstract Single<User> getCurrentUser(int id);

    @Transaction
    @Query("SELECT * FROM dictionary_word WHERE word = :word")
    public abstract Single<WordDefinitions> getWordData(String word);
}
