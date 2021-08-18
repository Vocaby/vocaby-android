package com.vocaby.app.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Response;
import com.vocaby.app.WordService;
import com.vocaby.app.models.Word;
import com.vocaby.app.repositories.DictionaryRepository;

import org.json.JSONObject;

public class DictionaryViewModel extends AndroidViewModel {
    private final DictionaryRepository dictionaryRepository;
    private final MutableLiveData<Word> mWordData;
    private String searchedWord;
    private final MutableLiveData<String> search;

    public DictionaryViewModel(Application application) {
        super(application);
        search = new MutableLiveData<>();
        mWordData = new MutableLiveData<>();
        dictionaryRepository = new DictionaryRepository(application);
    }

    public LiveData<String> getSearch() {
        return search;
    }

    public void setSearch(String word) {
        if(search.getValue() == null) {
            search.setValue(word);
        } else {
            if(!search.getValue().equals(word)) {
                search.setValue(word);
            }
        }
    }

    public LiveData<Word> getWordData() {
        return mWordData;
    }

    public void retrieveWordDataFromRepo(String word, boolean isConnectedToInternet) {
        searchedWord = word;
        Word wordData = dictionaryRepository.getWordDataFromDatabase(word);
        if(wordData == null) {
            if(isConnectedToInternet) {
                dictionaryRepository.getWordDataFromApi(word, apiListener);
            } else {
                mWordData.postValue(new Word(word));
            }
        } else {
            mWordData.postValue(wordData);
        }
    }

    private final Response.Listener<JSONObject> apiListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            WordService service = new WordService(searchedWord, response);
            service.parse();
            if(service.wasSuccessful()) {
                Word wordData = service.getWordData();
                if(wordData == null) {
                    wordData = new Word(searchedWord);
                }

                mWordData.postValue(wordData);
            } else {
                mWordData.postValue(new Word(searchedWord));
            }
        }
    };}
