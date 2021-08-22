package com.vocaby.app.api;

import com.vocaby.app.models.WordModel;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VocabyApiService {
    @Headers("Vocaby-Api-Key: dCKMPJZW.8m3GHcR3wU4d9Tkg7Hy2eX3ujtmah1kc")
    @GET("dictionary/{word}")
    Single<WordModel> getWordData(@Path("word") String word);

    @Headers("Vocaby-Api-Key: dCKMPJZW.8m3GHcR3wU4d9Tkg7Hy2eX3ujtmah1kc")
    @POST("login/")
    Single<AuthResponse> login(@Body LoginRequest loginRequest);

    @Headers("Vocaby-Api-Key: dCKMPJZW.8m3GHcR3wU4d9Tkg7Hy2eX3ujtmah1kc")
    @POST("logout/")
    Completable logout(@Header("Authorization") String token);

    @Headers("Vocaby-Api-Key: dCKMPJZW.8m3GHcR3wU4d9Tkg7Hy2eX3ujtmah1kc")
    @POST("register/")
    Single<AuthResponse> register(@Body RegisterRequest registerRequest);
}
