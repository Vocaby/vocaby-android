package com.vocaby.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.vocaby.app.data.dao.VocabyDao;
import com.vocaby.app.data.entity.CustomDefinition;
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.data.entity.CustomEntryGroup;
import com.vocaby.app.data.entity.Definition;
import com.vocaby.app.data.entity.Type;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.data.entity.UserSave;
import com.vocaby.app.data.entity.Word;

@Database(entities = {
        Word.class, User.class, Definition.class, UserSave.class,
        CustomEntry.class, CustomEntryGroup.class, CustomDefinition.class, Type.class},
        version = 1, exportSchema = false)
public abstract class VocabyDatabase extends RoomDatabase {
    public abstract VocabyDao vocabyDao();
    private static volatile VocabyDatabase INSTANCE;

    public static VocabyDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (VocabyDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            VocabyDatabase.class, "database")
                            .createFromAsset("databases/database.db")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
