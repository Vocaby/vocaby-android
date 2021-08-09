package com.vocaby.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vocaby.app.Word;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseManager databaseManager;

    private DatabaseManager(Context ctx) {
        this.openHelper = new DatabaseOpener(ctx);
    }

    public static DatabaseManager getInstance(Context ctx) {
        if(databaseManager == null) {
            Log.d("Database", "Creating new database instance");
            databaseManager = new DatabaseManager(ctx);
        } else {
            Log.d("Database", "Returning database instance");
        }

        return databaseManager;
    }

    public boolean isOpen() {
        if(database != null) {
            return database.isOpen();
        }

        return false;
    }

    public void openDatabase() {
        this.database = openHelper.getReadableDatabase();
    }

    public void closeDatabase() {
        if (database != null) {
            this.database.close();
        }
    }

    public Word getWordData(String word) {
        Word wordData = null;
        if(database != null) {
            String sql = "SELECT pronunciation, pos, definition, sentence FROM words as w, definitions as d WHERE w.id = d.word_id AND w.word = ?";
            Cursor cursor = database.rawQuery(sql, new String[] {word});
            if(cursor.getCount() > 0) {
                wordData = new Word(word);
                cursor.moveToFirst();
                String pronunciation = cursor.getString(0);
                wordData.setPronunciation(pronunciation);
                while (!cursor.isAfterLast()) {
                    String pos = cursor.getString(1);
                    String definition = cursor.getString(2);
                    String sentence = cursor.getString(3);

                    wordData.addDefinition(pos, definition);
                    wordData.addSentence(pos, sentence);
                    cursor.moveToNext();
                }
            }

            cursor.close();
        } else {
            throw new IllegalStateException("Database is not open");
        }

        return wordData;
    }

    public Word getRandomWordData(int id) {
        Word wordData = null;
        if(database != null) {
            String sql = "SELECT word, pronunciation, pos, definition, sentence FROM words as w, definitions as d WHERE w.id = d.word_id AND w.id = ?";
            Cursor cursor = database.rawQuery(sql, new String[] {String.valueOf(id)});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                String word = cursor.getString(0);
                String pronunciation = cursor.getString(1);
                wordData = new Word(word);
                wordData.setPronunciation(pronunciation);
                while (!cursor.isAfterLast()) {
                    String pos = cursor.getString(2);
                    String definition = cursor.getString(3);
                    String sentence = cursor.getString(4);

                    wordData.addDefinition(pos, definition);
                    wordData.addSentence(pos, sentence);
                    cursor.moveToNext();
                }
            }

            cursor.close();
        } else {
            throw new IllegalStateException("Database is not open");
        }

        return wordData;
    }

    public int getRandomWordIndex() {
        if(database != null) {
            String countQuery = "SELECT  * FROM words";
            Cursor cursor = database.rawQuery(countQuery, null);
            int count = cursor.getCount();
            int id = getRandomNumber(0, count);
            cursor.close();

            return id;
        } else {
            throw new IllegalStateException("Database is not open");
        }
    }

    private int getRandomNumber(int min, long max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
