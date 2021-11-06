package com.vocaby.app.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.vocaby.app.data.entity.CustomDefinition;
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.data.entity.CustomEntryGroup;
import com.vocaby.app.data.entity.EntryWithData;
import com.vocaby.app.data.entity.Type;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.data.entity.UserSaves;
import com.vocaby.app.data.entity.WordDefinitions;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public abstract class VocabyDao {
    @Query("SELECT word FROM dictionary_word UNION " +
            "SELECT entry FROM custom_user_entry " +
            "ORDER BY word COLLATE NOCASE ASC")
    public abstract Single<List<String>> getDictionaryEntries();

    @Query("SELECT EXISTS(SELECT 1 FROM dictionary_word WHERE word = :entry)")
    public abstract Single<Boolean> checkEntryExistence(String entry);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract Single<Long> createUser(User user);

    @Query("SELECT EXISTS(SELECT * FROM vocaby_user WHERE user_id = :id)")
    public abstract Single<Integer> checkUser(int id);

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

    @Query("SELECT word FROM saves WHERE user_id = :id ORDER BY id DESC")
    public abstract Single<List<String>> getSaves(int id);

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

    // CUSTOM ENTRY
    @Insert
    public abstract Single<Long> insertCustomEntry(CustomEntry customEntry);

    @Update
    public abstract Completable updateCustomEntry(CustomEntry customEntry);

    @Insert
    public abstract Single<List<Long>> insertCustomEntryGroups(List<CustomEntryGroup> customEntryGroups);

    @Delete
    public abstract Completable deleteCustomEntryGroups(List<CustomEntryGroup> customEntryGroups);

    @Update
    public abstract Completable updateCustomEntryGroups(List<CustomEntryGroup> customEntryGroups);

    @Insert
    public abstract Completable insertCustomDefinitions(List<CustomDefinition> customDefinitions);

    @Delete
    public abstract Completable deleteCustomDefinitions(List<CustomDefinition> customDefinitions);

    @Update
    public abstract Completable updateCustomDefinitions(List<CustomDefinition> customDefinitions);

    @Query("DELETE FROM custom_user_entry WHERE custom_entry_id = :entryId")
    public abstract Completable deleteUserEntry(int entryId);

    @Query("DELETE FROM custom_user_entry WHERE entry = :entry")
    public abstract Completable deleteUserEntry(String entry);

    @Transaction
    @Query("SELECT * FROM custom_user_entry WHERE user_id = :id AND entry = :entry")
    public abstract Single<EntryWithData> getUserEntryData(int id, String entry);

    @Query("SELECT entry FROM custom_user_entry WHERE user_id = :id ORDER BY last_updated DESC")
    public abstract Single<List<String>> getUserEntries(int id);

    @Query("DELETE FROM custom_user_entry WHERE user_id = :id")
    public abstract Completable clearUserEntries(int id);

    @Insert
    public abstract Completable insertTypes(List<Type> types);

    @Query("SELECT type from entry_type")
    public abstract Single<List<String>> getTypes();
}
