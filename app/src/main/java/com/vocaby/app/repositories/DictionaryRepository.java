package com.vocaby.app.repositories;

import android.app.Application;

import com.vocaby.app.api.ApiManager;
import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.database.DatabaseManager;
import com.vocaby.app.models.WordModel;

public class DictionaryRepository {
    private DatabaseManager databaseManager;
    private ApiManager apiManager;

    public DictionaryRepository(Application application) {
        databaseManager = DatabaseManager.getInstance(application);
        apiManager = ApiManager.getInstance();
    }

    public WordModel getWordDataFromDatabase(String word) {
        databaseManager.openDatabase();
        WordModel data = databaseManager.getWordData(word);
        databaseManager.closeDatabase();
        return data;
    }

    public VocabyApiService getVocabyApiService() {
        return apiManager.getVocabyApiService("DICTIONARY");
    }
}
