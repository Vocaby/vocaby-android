package com.vocaby.app.database;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseOpener extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "vocabydevdb.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseOpener(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
