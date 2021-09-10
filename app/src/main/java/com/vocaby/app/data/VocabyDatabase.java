package com.vocaby.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.vocaby.app.data.dao.VocabyDao;
import com.vocaby.app.data.entity.Definition;
import com.vocaby.app.data.entity.OfflineAddedSaves;
import com.vocaby.app.data.entity.OfflineRemovedSaves;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.data.entity.UserSaves;
import com.vocaby.app.data.entity.Word;

@Database(entities = {Word.class, User.class, Definition.class, UserSaves.class, OfflineAddedSaves.class, OfflineRemovedSaves.class}, version = 1, exportSchema = false)
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
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
