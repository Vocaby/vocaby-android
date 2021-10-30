package com.vocaby.app.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vocaby.app.Constants;
import com.vocaby.app.models.datapackage.EntryDataPackage;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    final String BASE_URL = Constants.VOCABY_API_SERVER;
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

    public VocabyApiService getVocabyApiService(int type) {
        apiService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(createGsonConverterFactory(type))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(VocabyApiService.class);

        return apiService;
    }

    private static GsonConverterFactory createGsonConverterFactory(int type) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // Adding custom deserializers
        switch(type) {
            case VocabyApiService.DEFINITION:
                gsonBuilder.registerTypeAdapter(EntryDataPackage.class, new GetWordDataDeserializer());
                break;
            case VocabyApiService.LOGIN:
                gsonBuilder.registerTypeAdapter(AuthResponse.class, new GetAuthDeserializer());
                break;
            default:
                return GsonConverterFactory.create();
        }

        Gson myGson = gsonBuilder.create();

        return GsonConverterFactory.create(myGson);
    }
}
