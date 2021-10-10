package com.vocaby.app.viewmodels;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.Constants;
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
    private List<String> customEntries;
    private final SingleLiveEvent<List<String>> mEntries;
    private final SharedPreferences userSharedPreference;
    private final SingleLiveEvent<Integer> mResult;

    private int selectedPosition;

    public MyEntryViewModel(@NonNull Application application) {
        super(application);
        userSharedPreference = getApplication().getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        mEntries = new SingleLiveEvent<>();
        mResult = new SingleLiveEvent<>();
        selectedPosition = 0;

        int currentId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1);
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

    public LiveData<List<String>> getEntries() {
        return mEntries;
    }

    public Intent addEntryDataToIntent(Intent intent, String entry, int position) {
        if (position == -1) {
            String other = StringFormatter.cleanText(entry);
            for (int i = 0; i < customEntries.size(); i++) {
                String e = StringFormatter.cleanText(customEntries.get(i));
                if (e.equals(other)) {
                    position = i;
                }
            }
        }

        selectedPosition = position;

        intent.putExtra(ENTRY_TEXT_KEY, entry);
        return intent;
    }

    public void handleResult(ActivityResult result) {
        mEntries.setValue(customEntries);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}
