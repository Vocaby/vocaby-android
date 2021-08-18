package com.vocaby.app.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Response;
import com.vocaby.app.api.ApiManager;
import com.vocaby.app.database.DatabaseManager;
import com.vocaby.app.models.Word;

import org.json.JSONObject;

public class DictionaryRepository {
    private DatabaseManager databaseManager;
    private ApiManager apiManager;

    public DictionaryRepository(Application application) {
        databaseManager = DatabaseManager.getInstance(application);
        apiManager = ApiManager.getInstance(application);
    }

    public Word getWordDataFromDatabase(String word) {
        databaseManager.openDatabase();
        Word data = databaseManager.getWordData(word);
        databaseManager.closeDatabase();
        return data;
    }

    public void getWordDataFromApi(String word, Response.Listener<JSONObject> apiListener) {
        apiManager.getWordData(word, apiListener);
    }
}
