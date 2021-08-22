package com.vocaby.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vocaby.app.models.WordModel;

public class DatabaseManager {
    private final SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static DatabaseManager databaseManager;
    private int openCounter;

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

    public void openDatabase() {
        openCounter++;
        if(openCounter == 1){
            database = openHelper.getReadableDatabase();
        }
    }

    public void closeDatabase() {
        openCounter--;
        if (database != null && openCounter == 0) {
            database.close();
        }
    }

    public WordModel getWordData(String word) {
        WordModel wordModelData = null;
        if(database != null) {
            String sql = "SELECT pronunciation, pos, definition, sentence FROM words as w, definitions as d WHERE w.id = d.word_id AND w.word = ?";
            Cursor cursor = database.rawQuery(sql, new String[] {word});
            if(cursor.getCount() > 0) {
                wordModelData = new WordModel(word);
                cursor.moveToFirst();
                String pronunciation = cursor.getString(0);
                wordModelData.setPronunciation(pronunciation);
                while (!cursor.isAfterLast()) {
                    String pos = cursor.getString(1);
                    String definition = cursor.getString(2);
                    String sentence = cursor.getString(3);

                    wordModelData.addDefinition(pos, definition);
                    wordModelData.addSentence(pos, sentence);
                    cursor.moveToNext();
                }
            }

            cursor.close();
        } else {
            throw new IllegalStateException("Database is not open");
        }

        return wordModelData;
    }

    public WordModel getRandomWordData(int id) {
        WordModel wordModelData = null;
        if(database != null) {
            String sql = "SELECT word, pronunciation, pos, definition, sentence FROM words as w, definitions as d WHERE w.id = d.word_id AND w.id = ?";
            Cursor cursor = database.rawQuery(sql, new String[] {String.valueOf(id)});
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                String word = cursor.getString(0);
                String pronunciation = cursor.getString(1);
                wordModelData = new WordModel(word);
                wordModelData.setPronunciation(pronunciation);
                while (!cursor.isAfterLast()) {
                    String pos = cursor.getString(2);
                    String definition = cursor.getString(3);
                    String sentence = cursor.getString(4);

                    wordModelData.addDefinition(pos, definition);
                    wordModelData.addSentence(pos, sentence);
                    cursor.moveToNext();
                }
            }

            cursor.close();
        } else {
            throw new IllegalStateException("Database is not open");
        }

        return wordModelData;
    }

    public int getRandomWordIndex() {
        if(database != null) {
            String countQuery = "SELECT  * FROM words";
            Cursor cursor = database.rawQuery(countQuery, null);
            int count = cursor.getCount();
            int id = getRandomNumber(count);
            cursor.close();

            return id;
        } else {
            throw new IllegalStateException("Database is not open");
        }
    }

    private int getRandomNumber(int max) {
        return (int) ((Math.random() * max));
    }
}
