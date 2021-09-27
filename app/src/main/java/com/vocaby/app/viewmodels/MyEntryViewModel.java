package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;
import com.vocaby.app.utils.StringFormatter;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MyEntryViewModel extends AndroidViewModel {
    public static final String ENTRY_TEXT_KEY = "ENTRY_TEXT";
    public static final String ENTRY_ID_KEY = "ENTRY_ID";
    public static final String ENTRY_DELETE = "ENTRY_DELETE";
    public static final String ENTRY_EDIT = "ENTRY_EDIT";

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;
    private List<CustomEntry> customEntries;
    private final SingleLiveEvent<List<CustomEntry>> mEntries;
    private final SharedPreferences userSharedPreference;

    private int selectedPosition;

    public MyEntryViewModel(@NonNull Application application) {
        super(application);
        userSharedPreference = getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        mEntries = new SingleLiveEvent<>();
        selectedPosition = 0;

        int currentId = userSharedPreference.getInt("CURRENT_USER_ID", 1);
        compositeDisposable.add(
                vocabyRepository.getUserEntries(currentId)
                        .subscribe(list -> {
                            customEntries = list;
                            mEntries.setValue(customEntries);
                        }, error -> {
                            Bugsnag.notify(error);
                            Log.d("vocabydebug", error.getMessage());
                        })
        );
    }

    public LiveData<List<CustomEntry>> getEntries() {
        return mEntries;
    }

    public Intent addEntryDataToIntent(Intent intent, String entry) {
        String other = StringFormatter.cleanText(entry);
        int entryId = -1;
        selectedPosition = 0;
        for (int i = 0; i < customEntries.size(); i++) {
            String e = StringFormatter.cleanText(customEntries.get(i).getEntry());
            if (e.equals(other)) {
                entryId = customEntries.get(i).getEntryId();
                selectedPosition = i;
            }
        }

        intent.putExtra(ENTRY_ID_KEY, entryId);
        intent.putExtra(ENTRY_TEXT_KEY, entry);
        return intent;
    }

    public Intent addEntryDataToIntent(Intent intent, CustomEntry entry) {
        intent.putExtra(ENTRY_ID_KEY, entry.getEntryId());
        intent.putExtra(ENTRY_TEXT_KEY, entry.getEntry());
        return intent;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
