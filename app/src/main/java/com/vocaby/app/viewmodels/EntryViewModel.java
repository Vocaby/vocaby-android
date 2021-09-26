package com.vocaby.app.viewmodels;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class EntryViewModel extends AndroidViewModel {
    public static final int ADD_GROUP = 0;
    public static final int EDIT_GROUP = 1;
    public static final int REMOVE_GROUP = 2;
    public static final int CREATE_ENTRY = 3;
    public static final int EMPTY_ENTRY = 4;
    public static final String GROUP_KEY = "GROUP_KEY";

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private EntryModel entryData;
    private final SingleLiveEvent<Integer> mResult;
    private final SingleLiveEvent<List<DefinitionGroupModel>> mGroups;
    private final SharedPreferences userSharedPreference;

    private int selectedItemPosition;

    public EntryViewModel(Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        userSharedPreference = getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);

        selectedItemPosition = 0;
        mGroups = new SingleLiveEvent<>();

        // Used to notify adapter
        mResult = new SingleLiveEvent<>();
    }

    public void getEntry(int entryId, String entry) {
        if (entryId > -1) {
            compositeDisposable.add(
                    vocabyRepository.getEntryData(entryId)
                            .subscribe(entryData -> {
                                        this.entryData = entryData;
                                        mGroups.setValue(entryData.getDefinitionGroups());
                                    }, error -> {
                                        error.printStackTrace();
                                        Bugsnag.notify(error);
                                    }
                            )
            );
        } else {
            entryData = new EntryModel(entry);
            mGroups.setValue(entryData.getDefinitionGroups());
        }
    }

    public Intent addGroupDataToIntent(Intent intent, int position) {
        intent.putExtra(GROUP_KEY, entryData.getDefinitionGroup(position));
        return intent;
    }

    public Intent addGroupDataToIntent(Intent intent, String type) {
        entryData.addDefinitionGroup(type);
        mGroups.setValue(entryData.getDefinitionGroups());
        intent.putExtra(GROUP_KEY, entryData.getLastGroup());
        return intent;
    }

    public Intent addResultDataToIntent(Intent intent) {
        intent.putExtra(MyEntryViewModel.ENTRY_TEXT_KEY, entryData.getEntry());
        intent.putExtra(MyEntryViewModel.ENTRY_ID_KEY, entryData.getId());

        return intent;
    }

    public boolean entryHasType(String type) {
        return entryData.hasGroup(type.toLowerCase());
    }

    public LiveData<List<DefinitionGroupModel>> getGroups() {
        return mGroups;
    }

    public void removeGroup(int position) {
        entryData.removeGroup(position);
        mGroups.setValue(entryData.getDefinitionGroups());
    }

    public LiveData<Integer> getResult() {
        return mResult;
    }

    public void setSelectedItemPosition(int position) {
        selectedItemPosition = position;
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void handleResult(ActivityResult result) {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData().getParcelableExtra(GROUP_KEY) instanceof DefinitionGroupModel) {
                DefinitionGroupModel definitionGroup = result.getData().getParcelableExtra(GROUP_KEY);
                String type = definitionGroup.getType();
                if (definitionGroup.isEmpty()) {
                    // Todo: Remove group from the local database
                    mResult.setValue(REMOVE_GROUP);
                } else {
                    if (entryData.hasGroup(type)) {
                        entryData.replaceDefinitionGroup(type, definitionGroup);
                        mResult.setValue(EDIT_GROUP);
                    } else {
                        entryData.addDefinitionGroup(definitionGroup);
                        mResult.setValue(ADD_GROUP);
                    }

                    mGroups.setValue(entryData.getDefinitionGroups());
                }
            }
        }
    }

    public String parseRetrieved(Intent intent) {
        String entry = intent.getStringExtra(MyEntryViewModel.ENTRY_TEXT_KEY);
        getEntry(intent.getIntExtra(MyEntryViewModel.ENTRY_ID_KEY, 1), entry);

        return entry;
    }

    public void createNewEntry() {
        if (entryData.getDefinitionGroups().isEmpty()) {
            mResult.setValue(EMPTY_ENTRY);
        } else {
            int currentId = userSharedPreference.getInt("CURRENT_USER_ID", 1);
            compositeDisposable.add(
                    vocabyRepository.insertNewEntry(currentId, entryData, System.currentTimeMillis())
                            .subscribe((id) -> {
                                entryData.setId(id);
                                mResult.setValue(CREATE_ENTRY);
                            }, error -> {
                                // TODO: Handle Unique Constraint Fails
                                Log.d("Vocabydebug", error.getMessage());
                            })
            );
        }
    }
}
