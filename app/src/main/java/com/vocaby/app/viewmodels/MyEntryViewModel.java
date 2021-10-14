package com.vocaby.app.viewmodels;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.vocaby.app.models.CustomEntryPackage;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.SingleLiveEvent;
import com.vocaby.app.utils.StringFormatter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class MyEntryViewModel extends AndroidViewModel {
    public static final String CUSTOM_ENTRY_PACKAGE_KEY = "CUSTOM_ENTRY";

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;
    private List<String> customEntries;
    private final SingleLiveEvent<List<String>> mEntries;
    private final MutableLiveData<Integer> mEntryCount;

    public MyEntryViewModel(@NonNull Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        mEntries = new SingleLiveEvent<>();
        mEntryCount = new MutableLiveData<>(0);

        compositeDisposable.add(
                vocabyRepository.getCurrentUserId()
                .flatMap(vocabyRepository::getUserEntries)
                .subscribe(list -> {
                    customEntries = new ArrayList<>(list);
                    mEntries.setValue(customEntries);
                    mEntryCount.setValue(customEntries.size());
                }, error -> {
                    Logger.reportError(error);
                    Log.d("vocabydebug", error.getMessage());
                })
        );
    }

    public LiveData<List<String>> getEntries() {
        return mEntries;
    }

    public LiveData<Integer> getCustomEntryCount() {
        return mEntryCount;
    }

    public Intent addEntryDataToIntent(Intent intent, String entry, int position) {
        int selectedPosition;
        if (position == -1) {
            String other = StringFormatter.cleanText(entry);
            selectedPosition = customEntries.indexOf(other);
        } else {
            selectedPosition = position;
        }

        CustomEntryPackage customEntryPackage;
        if (selectedPosition == -1) {
            customEntryPackage = new CustomEntryPackage(entry, false);
        } else {
            customEntryPackage = new CustomEntryPackage(entry, true);
        }

        intent.putExtra(CUSTOM_ENTRY_PACKAGE_KEY, customEntryPackage);
        return intent;
    }

    public void handleResult(ActivityResult result) {
        mEntryCount.setValue(customEntries.size());
    }
}
