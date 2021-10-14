package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.vocaby.app.models.EntryModel;
import com.vocaby.app.models.SearchSuggestionItem;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.Logger;
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
    private final SingleLiveEvent<EntryModel> mWordModel;
    private final CompositeDisposable compositeDisposable;
    private final MutableLiveData<List<String>> searchHistory;
    private final MutableLiveData<Integer> mEntryCount;
    private final VocabyRepository vocabyRepository;
    private final MutableLiveData<List<String>> mDictionaryEntries;

    public DictionaryViewModel(Application application) {
        super(application);
        search = new SingleLiveEvent<>();
        searchStack = new Stack<>();
        mWordModel = new SingleLiveEvent<>();
        compositeDisposable = new CompositeDisposable();
        vocabyRepository = new VocabyRepository(getApplication());
        mDictionaryEntries = new MutableLiveData<>(new ArrayList<>());
        mEntryCount = new MutableLiveData<>(0);

        searchHistory = new MutableLiveData<>();
        searchHistory.setValue(vocabyRepository.getHistory());
    }

    public void populateDictionaryEntries() {
        compositeDisposable.add(
                vocabyRepository.populateDictionaryEntries()
                        .subscribe(entries -> {
                            mDictionaryEntries.setValue(entries);
                            mEntryCount.setValue(entries.size());
                        }, Throwable::printStackTrace)
        );
    }

    public void resetDictionaryEntries() {
        if (mDictionaryEntries.getValue() != null) {
            mDictionaryEntries.setValue(mDictionaryEntries.getValue());
            mEntryCount.setValue(mDictionaryEntries.getValue().size());
        }
    }

    public LiveData<List<String>> getDictionaryEntries() {
        return mDictionaryEntries;
    }

    public LiveData<Integer> getEntryCount() {
        return mEntryCount;
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
                            }, error -> {
                                Logger.reportError(error);
                                Log.e("DictionaryViewModel: ", error.getMessage());
                            })
            );

            editor.putInt("appStarted", today);
            editor.apply();
        } else {
            int id = randomWordPicker.getInt("randomWordId", 100000);
            compositeDisposable.add(
                    vocabyRepository.getWordDataFromDatabase(id)
                            .subscribe(mWordModel::setValue,
                                    error -> {
                                        Logger.reportError(error);
                                        Log.e("DictionaryViewModel: ", error.getMessage());
                                    })
            );
        }
    }



    public List<SearchSuggestionItem> getSearchSuggestion(String newQuery, int threshold) {
        List<SearchSuggestionItem> searchSuggestions = new ArrayList<>();
        if (!newQuery.isEmpty() && mDictionaryEntries.getValue() != null) {
            int index = VocabyAlgo.BinarySearchPrefix(mDictionaryEntries.getValue(), newQuery);
            if (index > -1 && index < mDictionaryEntries.getValue().size()) {
                Iterator<String> it = mDictionaryEntries.getValue().listIterator(index);
                int count = 0;
                while (it.hasNext() && count < threshold) {
                    String entry = it.next();
                    if (entry.toLowerCase().contains(newQuery)) {
                        searchSuggestions.add(new SearchSuggestionItem(entry));
                    }

                    count++;
                    index++;
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

    public LiveData<EntryModel> getRandomWord() {
        return mWordModel;
    }

    public void writeHistory(String word) {
        List<String> newHistory = vocabyRepository.writeHistory(word);
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
