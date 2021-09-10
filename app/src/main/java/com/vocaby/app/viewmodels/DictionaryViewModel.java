package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.data.DataManager;
import com.vocaby.app.data.entity.Definition;
import com.vocaby.app.data.entity.WordDefinitions;
import com.vocaby.app.models.SearchSuggestionItem;
import com.vocaby.app.models.WordModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;
import com.vocaby.app.utils.VocabyAlgo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DictionaryViewModel extends AndroidViewModel {
    private final SingleLiveEvent<String> search;
    private final Stack<String> searchStack;
    private final SingleLiveEvent<WordModel> mWordModel;
    private final CompositeDisposable compositeDisposable;
    private final DataManager dataManager;
    private final MutableLiveData<List<String>> searchHistory;
    private final VocabyRepository vocabyRepository;
    private List<String> dictionaryEntries;

    public DictionaryViewModel(Application application) {
        super(application);
        search = new SingleLiveEvent<>();
        searchStack = new Stack<>();
        mWordModel = new SingleLiveEvent<>();
        compositeDisposable = new CompositeDisposable();
        dataManager = DataManager.getInstance(application);
        searchHistory = new MutableLiveData<>();
        searchHistory.setValue(dataManager.getHistory());
        vocabyRepository = new VocabyRepository(getApplication());
        dictionaryEntries = new ArrayList<>();
    }

    public void setupDictionary() {
        compositeDisposable.add(
                vocabyRepository.getDictionaryEntries()
                        .subscribe(entries -> dictionaryEntries = entries,
                                Throwable::printStackTrace)
        );
    }

    public void updateRandomWord() {
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
                            }, error -> {
                                Bugsnag.notify(error);
                                Log.e("DictionaryViewModel: ", error.getMessage());
                            })
            );

            editor.putInt("appStarted", today);
            editor.apply();
        } else {
            int id = randomWordPicker.getInt("randomWordId", 100000);
            compositeDisposable.add(
                    vocabyRepository.getWordDataFromDatabase(id)
                            .subscribe(wordDefinitions -> mWordModel.setValue(makeWordData(wordDefinitions)),
                                    error -> {
                                        Bugsnag.notify(error);
                                        Log.e("DictionaryViewModel: ", error.getMessage());
                                    })
            );
        }
    }

    private WordModel makeWordData(WordDefinitions wordDefinitions) {
        WordModel wordData = new WordModel(wordDefinitions.word.getWord());
        if (wordDefinitions.word.getPronunciation() != null) {
            wordData.setPronunciation(wordDefinitions.word.getPronunciation());
        } else {
            wordData.setPronunciation("");
        }

        for (Definition data : wordDefinitions.definitions) {
            wordData.addDefinition(data.getPos(), data.getDefinition());
            wordData.addSentence(data.getPos(), data.getSentence());
        }

        return wordData;
    }

    public List<SearchSuggestionItem> getSearchSuggestion(String newQuery, int threshold) {
        List<SearchSuggestionItem> searchSuggestions = new ArrayList<>();
        if (!newQuery.isEmpty()) {
            int index = VocabyAlgo.BinarySearchPrefix(dictionaryEntries, newQuery);
            if (index > -1 && index < dictionaryEntries.size()) {
                Iterator<String> it = dictionaryEntries.listIterator(index);
                int count = 0;
                while (it.hasNext() && count < threshold) {
                    String entry = it.next();
                    if (entry.contains(newQuery)) {
                        searchSuggestions.add(new SearchSuggestionItem(entry));
                    }

                    count++;
                }

                return searchSuggestions;
            }
        }

        return searchSuggestions;
    }

    public LiveData<String> getSearch() {
        return search;
    }

    public void setSearch(String word) {
        if (!word.isEmpty()) {
            search.setValue(word);
        }
    }

    public boolean isOpen(String word) {
        if (searchStack.empty()) {
            return false;
        } else {
            return searchStack.peek().equals(word);
        }
    }

    public void addToStack(String word) {
        searchStack.push(word);
    }

    public void popSearchStack() {
        if (!searchStack.empty()) {
            searchStack.pop();
        }
    }

    public LiveData<WordModel> getRandomWord() {
        return mWordModel;
    }

    public void writeHistory(String word) {
        List<String> newHistory = dataManager.writeHistory(word);
        searchHistory.setValue(newHistory);
    }

    public LiveData<List<String>> getSearchHistory() {
        return searchHistory;
    }

    public String getHistoryWord(int position) {
        if (searchHistory.getValue() != null) {
            return searchHistory.getValue().get(position);
        }

        return "";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
