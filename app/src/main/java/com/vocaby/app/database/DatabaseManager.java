package com.vocaby.app.database;

import android.content.Context;
import android.database.Cursor;
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
}
