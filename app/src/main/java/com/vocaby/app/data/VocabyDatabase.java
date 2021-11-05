package com.vocaby.app.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.vocaby.app.data.dao.VocabyDao;
import com.vocaby.app.data.entity.CustomDefinition;
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.data.entity.CustomEntryGroup;
import com.vocaby.app.data.entity.Definition;
import com.vocaby.app.data.entity.Type;
import com.vocaby.app.data.entity.User;
import com.vocaby.app.data.entity.UserSaves;
import com.vocaby.app.data.entity.Word;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

@Database(entities = {
        Word.class, User.class, Definition.class, UserSaves.class,
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
                            .setJournalMode(JournalMode.TRUNCATE)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    prepopulateData(getDatabase(context).vocabyDao());
                                    super.onCreate(db);
                                }
                            }).build();
                }
            }
        }

        return INSTANCE;
    }

    private static void prepopulateData(VocabyDao vocabyDao) {
        List<Type> types = new ArrayList<>();
        types.add(new Type("noun"));
        types.add(new Type("verb"));
        types.add(new Type("adjective"));
        types.add(new Type("adverb"));
        types.add(new Type("idiom"));
        types.add(new Type("proverb"));
        types.add(new Type("phrase"));
        types.add(new Type("preposition"));
        types.add(new Type("interjection"));
        types.add(new Type("conjunction"));
        types.add(new Type("pronoun"));
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        Executors.newSingleThreadExecutor().execute(() ->
                compositeDisposable.add(
                    vocabyDao.insertTypes(types).subscribe(() -> {
                }, Throwable::printStackTrace)
        ));
    }
}
