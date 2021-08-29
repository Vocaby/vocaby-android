package com.vocaby.app.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vocaby.app.database.dao.VocabyDao;
import com.vocaby.app.database.entity.Definition;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.database.entity.UserSaves;
import com.vocaby.app.database.entity.Word;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Database(entities = {Word.class, User.class, Definition.class, UserSaves.class}, version = 2, exportSchema = false)
public abstract class VocabyDatabase extends RoomDatabase {
    public abstract VocabyDao vocabyDao();

    private static volatile VocabyDatabase INSTANCE;
    public static VocabyDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (VocabyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            VocabyDatabase.class, "vocaby_database")
                            .createFromAsset("databases/vocabydevdb.db")
                            // .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
