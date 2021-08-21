package com.vocaby.app.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.models.Word;
import com.vocaby.app.repositories.DictionaryRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DictionaryViewModel extends AndroidViewModel {
    private final DictionaryRepository dictionaryRepository;
    private final MutableLiveData<Word> mWordData;
    private final CompositeDisposable compositeDisposable;
    private final MutableLiveData<String> search;

    public DictionaryViewModel(Application application) {
        super(application);
        search = new MutableLiveData<>();
        mWordData = new MutableLiveData<>();
        dictionaryRepository = new DictionaryRepository(application);
        compositeDisposable = new CompositeDisposable();
    }

    public LiveData<String> getSearch() {
        return search;
    }

    public void setSearch(String word) {
        if (search.getValue() == null) {
            search.setValue(word);
        } else {
            if (!search.getValue().equals(word)) {
                search.setValue(word);
            }
        }
    }

    public LiveData<Word> getWordData() {
        return mWordData;
    }

    public void retrieveWordDataFromRepo(String word, boolean isConnectedToInternet) {
        Word wordData = dictionaryRepository.getWordDataFromDatabase(word);
        if (wordData == null) {
            if (isConnectedToInternet) {
                // Get definition from the api
                VocabyApiService vocabyApi = dictionaryRepository.getVocabyApiService();
                compositeDisposable.add(
                    vocabyApi.getWordData(word)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    mWordData::setValue,
                                    onError -> Log.e("DVM", onError.getMessage())
                            )
                );
            } else {
                // No definition in the database
                mWordData.setValue(new Word(word));
            }
        } else {
            // Get definition in the database
            mWordData.setValue(wordData);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
