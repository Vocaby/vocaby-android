package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

import com.vocaby.app.database.entity.Definition;
import com.vocaby.app.database.entity.WordDefinitions;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.Calendar;
import java.util.Stack;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DictionaryViewModel extends AndroidViewModel {
    private final SingleLiveEvent<String> search;
    private Stack<String> searchHistory;
    private SingleLiveEvent<WordModel> mWordModel;
    private CompositeDisposable compositeDisposable;

    public DictionaryViewModel(Application application) {
        super(application);
        search = new SingleLiveEvent<>();
        searchHistory = new Stack<>();
        mWordModel = new SingleLiveEvent<>();
        compositeDisposable = new CompositeDisposable();
    }

    public void updateRandomWord() {
        VocabyRepository vocabyRepository = new VocabyRepository(getApplication());
        SharedPreferences randomWordPicker =
                PreferenceManager.getDefaultSharedPreferences(getApplication());
        SharedPreferences.Editor editor = randomWordPicker.edit();
        int lastTimeStarted = randomWordPicker.getInt("appStarted", -1);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);

        if (today != lastTimeStarted) {
            compositeDisposable.add(
                vocabyRepository.getRandomWord()
                        .subscribe(wordDefinitions -> {
                            editor.putInt("randomWordId", wordDefinitions.word.getId());
                            editor.apply();
                            mWordModel.setValue(makeWordData(wordDefinitions));
                        }, error -> Log.e("DictionaryViewModel: ", error.getMessage()))
            );

            editor.putInt("appStarted", today);
            editor.apply();
        } else {
            int id = randomWordPicker.getInt("randomWordId", 100000);
            compositeDisposable.add(
                vocabyRepository.getWordDataFromDatabase(id)
                        .subscribe(wordDefinitions -> {
                            mWordModel.setValue(makeWordData(wordDefinitions));
                        }, error -> Log.e("DictionaryViewModel: ", error.getMessage()))
            );
        }
    }

    private WordModel makeWordData(WordDefinitions wordDefinitions) {
        WordModel wordData = new WordModel(wordDefinitions.word.getWord());
        if(wordDefinitions.word.getPronunciation() != null) {
            wordData.setPronunciation(wordDefinitions.word.getPronunciation());
        } else {
            wordData.setPronunciation("");
        }

        for(Definition data : wordDefinitions.definitions) {
            wordData.addDefinition(data.getPos(), data.getDefinition());
            wordData.addSentence(data.getPos(), data.getSentence());
        }

        return wordData;
    }

    public LiveData<String> getSearch() {
        return search;
    }

    public void setSearch(String word) {
        if(!word.isEmpty()) {
            if(searchHistory.size() == 0 || !searchHistory.peek().equals(word)) {
                search.setValue(word);
            }
        }
    }

    public void addToStack(String word) {
        searchHistory.push(word);
    }

    public void popSearchHistory() {
        if(!searchHistory.empty()) {
            searchHistory.pop();
        }
    }

    public LiveData<WordModel> getRandomWord() {
        return mWordModel;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
