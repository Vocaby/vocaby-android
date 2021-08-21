package com.vocaby.app.api;

import android.content.Context;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vocaby.app.models.Word;

import org.json.JSONObject;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    String BASE_URL = "https://django.ontarion.net/";
    private static ApiManager instance;
    private static VocabyApiService apiService;

    private ApiManager() {
    }

    public static synchronized ApiManager getInstance() {
        if (null == instance) {
            instance = new ApiManager();
        }

        return instance;
    }

    public VocabyApiService getVocabyApiService() {
        if(apiService == null) {
            apiService = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(createGsonConverterFactory())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build()
                    .create(VocabyApiService.class);
        }

        return apiService;
    }

    private static GsonConverterFactory createGsonConverterFactory() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // Adding custom deserializers
        gsonBuilder.registerTypeAdapter(Word.class, new GetWordDataDeserializer());
        Gson myGson = gsonBuilder.create();

        return GsonConverterFactory.create(myGson);
    }
}
