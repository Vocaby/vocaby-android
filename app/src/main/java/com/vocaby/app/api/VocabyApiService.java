package com.vocaby.app.api;

import com.vocaby.app.models.Word;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface VocabyApiService {
    @Headers("Vocaby-Api-Key: dCKMPJZW.8m3GHcR3wU4d9Tkg7Hy2eX3ujtmah1kc")
    @GET("dictionary/{word}")
    Observable<Word> getWordData(@Path("word") String word);
}
