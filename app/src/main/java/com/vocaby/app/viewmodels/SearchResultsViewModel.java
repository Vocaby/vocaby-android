package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.database.entity.Definition;
import com.vocaby.app.database.entity.User;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.NetworkManager;
import com.vocaby.app.utils.SingleLiveEvent;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchResultsViewModel extends AndroidViewModel {
    private final CompositeDisposable compositeDisposable;
    private final VocabyRepository vocabyRepository;
    private final SingleLiveEvent<WordModel> mWordData;

    public SearchResultsViewModel(@NonNull Application application) {
        super(application);
        compositeDisposable = new CompositeDisposable();
        vocabyRepository = new VocabyRepository(application);
        mWordData = new SingleLiveEvent<>();
    }

    public LiveData<WordModel> getWordData() {
        return mWordData;
    }

    public void retrieveWordDataFromRepo(String searched) {
        compositeDisposable.add(
                vocabyRepository.getWordDataFromDatabase(searched)
                        .subscribe(wordDefinitions -> {
                            WordModel wordData = new WordModel(searched);
                            if(wordDefinitions.word.getPronunciation() != null) {
                                wordData.setPronunciation(wordDefinitions.word.getPronunciation());
                            } else {
                                wordData.setPronunciation("");
                            }

                            for(Definition data : wordDefinitions.definitions) {
                                wordData.addDefinition(data.getPos(), data.getDefinition());
                                wordData.addSentence(data.getPos(), data.getSentence());
                            }

                            mWordData.setValue(wordData);
                        }, error -> {
                            if(error instanceof EmptyResultSetException) {
                                mWordData.setValue(new WordModel(searched));
                            }
                        })
        );

//        if (wordModelData == null) {
//            if (isConnectedToInternet) {
//                // Get definition from the api
//                VocabyApiService vocabyApi = vocabyRepository.getVocabyApiService();
//                compositeDisposable.add(
//                    vocabyApi.getWordData(searched)
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(
//                                    // onSuccess
//                                    mWordData::setValue,
//                                    // onError
//                                    onError -> Log.e("DVM", onError.getMessage())
//                            )
//                );
//            } else {
//                // No definition in the database
//                mWordData.setValue(new WordModel(searched));
//            }
//        } else {
//            // Get definition in the database
//            mWordData.setValue(wordModelData);
//        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
