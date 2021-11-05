package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.models.SearchSuggestionItem;
import com.vocaby.app.models.dictionary.EntryModel;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.SingleLiveEvent;
import com.vocaby.app.utils.VocabyAlgo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.vocaby.app.utils.StringFormatter.cleanText;

public class DictionaryViewModel extends AndroidViewModel {
    private final int SEARCH_SUGGESTIONS_SIZE = 4;

    private final MutableLiveData<String> mSearchedEntry;
    private final MutableLiveData<List<String>> searchHistory;
    private final SingleLiveEvent<EntryModel> mWordModel;
    private final SingleLiveEvent<List<SearchSuggestionItem>> mSearchSuggestions;
    private final SingleLiveEvent<Boolean> mSuggestionsRetrieveStatus;

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private List<String> searchStringEntries;
    private String searchQuery;

    // TODO: Allow multiple SearchResults fragment on top of each other
    private final Stack<String> searchStack;

    public DictionaryViewModel(Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(getApplication());
        compositeDisposable = new CompositeDisposable();
        searchStack = new Stack<>();
        searchStringEntries = new ArrayList<>();
        searchQuery = "";


        mSearchedEntry = new MutableLiveData<>();
        searchHistory = new MutableLiveData<>();
        mWordModel = new SingleLiveEvent<>();
        mSearchSuggestions = new SingleLiveEvent<>();
        mSuggestionsRetrieveStatus = new SingleLiveEvent<>();

        searchHistory.setValue(vocabyRepository.getHistory());
    }

    public LiveData<String> getSearch() {
        return mSearchedEntry;
    }
    public LiveData<EntryModel> getRandomWord() {
        return mWordModel;
    }
    public LiveData<List<String>> getSearchHistory() {
        return searchHistory;
    }
    public LiveData<List<SearchSuggestionItem>> getSearchSuggestions() { return mSearchSuggestions; }
    public LiveData<Boolean> getSuggestionRetrieveStatus() { return mSuggestionsRetrieveStatus; }

    public void setupDictionaryEntries() {
        compositeDisposable.add(
                vocabyRepository.setupDictionaryEntries()
                    .subscribe(() -> {}, Throwable::printStackTrace)
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
                            .subscribe(wordData -> {
                                editor.putInt("randomWordId", wordData.getId());
                                editor.apply();
                                mWordModel.setValue(wordData);
                            }, Logger::reportError)
            );

            editor.putInt("appStarted", today);
            editor.apply();
        } else {
            int id = randomWordPicker.getInt("randomWordId", 100000);
            compositeDisposable.add(
                    vocabyRepository.getWordDataFromDatabase(id)
                            .subscribe(mWordModel::setValue, Logger::reportError)
            );
        }
    }

    public void getSearchSuggestions(String oldQuery, String newQuery) {
        if (oldQuery.length() == 1 && newQuery.isEmpty()) {
            mSearchSuggestions.setValue(new ArrayList<>());
            searchStringEntries = null;
            searchQuery = "";
        } else if (oldQuery.isEmpty() && newQuery.length() == 1) {
            mSuggestionsRetrieveStatus.setValue(false);
            String initialCharacter = newQuery.substring(0, 1);
            compositeDisposable.add(
                    vocabyRepository.getEntriesByCharacter(initialCharacter)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(entries -> {
                            mSuggestionsRetrieveStatus.setValue(true);
                            searchStringEntries = entries;
                            if (searchQuery.isEmpty()) searchQuery = newQuery;
                            setSearchSuggestionItems(SEARCH_SUGGESTIONS_SIZE);
                        }, Throwable::printStackTrace)
            );
        } else {
            searchQuery = newQuery;
            setSearchSuggestionItems(SEARCH_SUGGESTIONS_SIZE);
        }
    }

    public void resetDictionaryEntries() {
        if (!searchQuery.isEmpty()) {
            String initialCharacter = searchQuery.substring(0, 1);
            compositeDisposable.add(
                    vocabyRepository.getEntriesByCharacter(initialCharacter)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(entries -> {
                                searchStringEntries = entries;
                            }, Throwable::printStackTrace)
            );
        }

    }

    private void setSearchSuggestionItems(int threshold) {
        List<SearchSuggestionItem> searchSuggestions = new ArrayList<>();

        if (searchStringEntries != null) {
            int index = VocabyAlgo.BinarySearchPrefix(searchStringEntries, searchQuery);
            if (index > -1 && index < searchStringEntries.size()) {
                Iterator<String> it = searchStringEntries.listIterator(index);
                int count = 0;
                while (it.hasNext() && count < threshold) {
                    String entry = it.next();
                    if (entry.contains(searchQuery)) {
                        searchSuggestions.add(new SearchSuggestionItem(entry));
                    }

                    count++;
                    index++;
                }
            }
        }

        mSearchSuggestions.setValue(searchSuggestions);
    }

    public void setSearch(String entry) {
        entry = cleanText(entry);
        if (!entry.isEmpty()) {
            if (!isOpen(entry)) {
                addToStack(entry);
                writeHistory(entry);
                mSearchedEntry.setValue(entry);
            }
        }
    }

    public boolean isOpen(String entry) {
        if (!searchStack.empty()) {
            if (searchStack.peek().equals(entry)) {
                return true;
            }

            popSearchStack();
        }

        return false;
    }

    public void addToStack(String word) {
        searchStack.push(word);
    }

    public void popSearchStack() {
        if (!searchStack.empty()) {
            searchStack.pop();
        }
    }

    public void writeHistory(String word) {
        List<String> newHistory = vocabyRepository.writeHistory(word);
        searchHistory.setValue(newHistory);
    }

    public void getHistoryDefinition(int position) {
        if (searchHistory.getValue() != null) {
            setSearch(searchHistory.getValue().get(position));
        } else {
            setSearch("");
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
