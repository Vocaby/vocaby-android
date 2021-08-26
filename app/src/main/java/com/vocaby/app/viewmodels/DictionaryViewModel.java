package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.rxjava3.EmptyResultSetException;

import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.database.entity.Definition;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.Stack;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DictionaryViewModel extends AndroidViewModel {
    private final VocabyRepository vocabyRepository;
    private final SingleLiveEvent<WordModel> mWordData;
    private final CompositeDisposable compositeDisposable;
    private final SingleLiveEvent<String> search;
    private Stack<String> searchHistory;
    private SharedPreferences sharedPreferences;
    private final String ID_KEY = "USER_ID";

    public DictionaryViewModel(Application application) {
        super(application);
        search = new SingleLiveEvent<>();
        mWordData = new SingleLiveEvent<>();
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        searchHistory = new Stack<>();
        sharedPreferences = application.getSharedPreferences(ID_KEY, Context.MODE_PRIVATE);
    }

    public LiveData<String> getSearch() {
        return search;
    }

    public void setSearch(String word) {
        if(!word.isEmpty()) {
            if(searchHistory.size() == 0 || !searchHistory.peek().equals(word)) {
                search.setValue(word);
                searchHistory.push(word);
            }
        }
    }

    public void popSearchHistory() {
        if(!searchHistory.empty()) {
            searchHistory.pop();
        }
    }

    public LiveData<WordModel> getWordData() {
        return mWordData;
    }

    public void saveWord(String word) {
        compositeDisposable.add(
            vocabyRepository.getCurrentUser(sharedPreferences.getInt(ID_KEY, 0))
                .subscribe(
                    user -> {
                        String token = user.getToken();
                        if(!token.isEmpty()) {
                            vocabyRepository.getVocabyApiService("")
                                .save("Token " + token, word)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {

                                }, error -> Log.e("saveWord: ", error.getMessage()));
                        }
                    }
                )
        );
    }

    public void retrieveWordDataFromRepo(String searched, boolean isConnectedToInternet) {
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
