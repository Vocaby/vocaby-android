package com.vocaby.app.api;

import com.vocaby.app.models.OfflineDataModel;
import com.vocaby.app.models.EntryDataPackage;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VocabyApiService {
    int DEFAULT = 0;
    int DEFINITION = 1;
    int LOGIN = 2;

    @Headers("Vocaby-Api-Key: CXPQmDpU.dSA8RCV0BdwsULIoMPjwDAHmmk4jMI3S")
    @POST("authdictionary/{word}")
    Single<EntryDataPackage> getWordData(@Header("Authorization") String token, @Path("word") String word);

    @Headers("Vocaby-Api-Key: CXPQmDpU.dSA8RCV0BdwsULIoMPjwDAHmmk4jMI3S")
    @POST("login/")
    Single<AuthResponse> login(@Body LoginRequest loginRequest);

    @Headers("Vocaby-Api-Key: CXPQmDpU.dSA8RCV0BdwsULIoMPjwDAHmmk4jMI3S")
    @POST("logout/")
    Completable logout(@Header("Authorization") String token);

    @Headers("Vocaby-Api-Key: CXPQmDpU.dSA8RCV0BdwsULIoMPjwDAHmmk4jMI3S")
    @POST("register/")
    Single<AuthResponse> register(@Body RegisterRequest registerRequest);

    @Headers("Vocaby-Api-Key: CXPQmDpU.dSA8RCV0BdwsULIoMPjwDAHmmk4jMI3S")
    @POST("account/saves/{word}")
    Completable save(@Header("Authorization") String token, @Path("word") String word);

    @Headers("Vocaby-Api-Key: CXPQmDpU.dSA8RCV0BdwsULIoMPjwDAHmmk4jMI3S")
    @DELETE("account/saves/{word}")
    Completable removeSave(@Header("Authorization") String token, @Path("word") String word);

    @Headers("Vocaby-Api-Key: CXPQmDpU.dSA8RCV0BdwsULIoMPjwDAHmmk4jMI3S")
    @GET("account/saves/")
    Single<List<String>> getSaves(@Header("Authorization") String token);

    @Headers("Vocaby-Api-Key: CXPQmDpU.dSA8RCV0BdwsULIoMPjwDAHmmk4jMI3S")
    @POST("account/sync/")
    Completable syncData(@Header("Authorization") String token, @Body OfflineDataModel<String> offlineDataModel);
}
