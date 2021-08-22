package com.vocaby.app.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocaby.app.api.VocabyApiService;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.repositories.DictionaryRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DictionaryViewModel extends AndroidViewModel {
    private final DictionaryRepository dictionaryRepository;
    private final MutableLiveData<WordModel> mWordData;
    private final CompositeDisposable compositeDisposable;
    private final SingleLiveEvent<String> search;

    public DictionaryViewModel(Application application) {
        super(application);
        search = new SingleLiveEvent<>();
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

    public LiveData<WordModel> getWordData() {
        return mWordData;
    }

    public void retrieveWordDataFromRepo(String searched, boolean isConnectedToInternet) {
        Log.d("Retrieving", searched);
        WordModel wordModelData = dictionaryRepository.getWordDataFromDatabase(searched);
        if (wordModelData == null) {
            if (isConnectedToInternet) {
                // Get definition from the api
                VocabyApiService vocabyApi = dictionaryRepository.getVocabyApiService();
                compositeDisposable.add(
                    vocabyApi.getWordData(searched)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    // onSuccess
                                    wordData -> {
                                        Log.d("GotFromApi", wordData.getWord());
                                        mWordData.setValue(wordData);
                                    },
                                    // onError
                                    onError -> Log.e("DVM", onError.getMessage())
                            )
                );
            } else {
                // No definition in the database
                mWordData.setValue(new WordModel(searched));
            }
        } else {
            Log.d("Got", wordModelData.getWord());
            // Get definition in the database
            mWordData.setValue(wordModelData);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
