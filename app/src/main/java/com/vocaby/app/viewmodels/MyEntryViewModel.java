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
import com.vocaby.app.data.entity.CustomEntry;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.ui.EntryBuilderActivity;
import com.vocaby.app.utils.SingleLiveEvent;
import com.vocaby.app.utils.StringFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MyEntryViewModel extends AndroidViewModel {
    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;
    private List<CustomEntry> customEntries;
    private final SingleLiveEvent<List<CustomEntry>> mEntries;

    private final SharedPreferences userSharedPreference;

    public MyEntryViewModel(@NonNull Application application) {
        super(application);
        userSharedPreference = getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        mEntries = new SingleLiveEvent<>();

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

    public Intent addEntryIdToIntent(Intent intent, String entry) {
        int id = customEntries.stream()
                .filter(customEntry -> {
                    String e = StringFormatter.cleanText(customEntry.getEntry());
                    String other = StringFormatter.cleanText(entry);
                    return e.equals(other);
                }).findFirst().orElse(new CustomEntry()).getEntryId();

        intent.putExtra("entryId", id);

        return intent;
    }

//    public void handleResult(ActivityResult result) {
//        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
//            int entryId = result.getData().getIntExtra("entryId", 1);
//            compositeDisposable.add(
//                    vocabyRepository.getEntryData(entryId)
//                            .subscribe(entryData -> {
//                                    List<CustomEntry> entries = mEntries.getValue();
//                                    if (entries != null) {
//                                        Log.d("vocabydebug", "" + entries.contains("a"));
//                                    }
//                                }, error -> Log.d("vocabydebug", error.getMessage())
//                            )
//            );
//        }
//    }
}
