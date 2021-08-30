package com.vocaby.app.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vocaby.app.models.WordDataPackage;
import com.vocaby.app.models.WordModel;

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

    public VocabyApiService getVocabyApiService(String type) {
        apiService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(createGsonConverterFactory(type))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(VocabyApiService.class);

        return apiService;
    }

    private static GsonConverterFactory createGsonConverterFactory(String type) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // Adding custom deserializers
        switch(type) {
            case "D":
                gsonBuilder.registerTypeAdapter(WordDataPackage.class, new GetWordDataDeserializer());
                break;
            case "L":
                gsonBuilder.registerTypeAdapter(AuthResponse.class, new GetAuthDeserializer());
                break;
            default:
                return GsonConverterFactory.create();
        }

        Gson myGson = gsonBuilder.create();

        return GsonConverterFactory.create(myGson);
    }
}
