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
import com.vocaby.app.Constants;
import com.vocaby.app.models.DefinitionChanges;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.models.GroupChanges;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class EntryViewModel extends AndroidViewModel {
    public static final int ADD_GROUP = 0;
    public static final int EDIT_GROUP = 1;
    public static final int REMOVE_GROUP = 2;
    public static final int SAVE_ENTRY = 3;
    public static final int EMPTY_ENTRY = 4;
    public static final int CANCEL = 5;
    public static final String GROUP_KEY = "GROUP_KEY";
    public static final String DEFINITION_CHANGES = "DEF_CHNGS";

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private EntryModel entryData;
    private List<DefinitionGroupModel> initialGroups;
    private final SingleLiveEvent<Integer> mResult;
    private final SingleLiveEvent<List<DefinitionGroupModel>> mGroups;
    private final SharedPreferences userSharedPreference;

    private int selectedItemPosition;
    private GroupChanges groupChanges;
    private Map<String, DefinitionChanges> definitionChangesMap;
    private boolean isEdit;
    private boolean shouldDelete;

    public EntryViewModel(Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        userSharedPreference = getApplication().getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE);

        selectedItemPosition = 0;
        mGroups = new SingleLiveEvent<>();
        definitionChangesMap = new HashMap<>();

        groupChanges = new GroupChanges(-1);
        initialGroups = new ArrayList<>();
        isEdit = false;
        shouldDelete = false;

        // Used to notify adapter
        mResult = new SingleLiveEvent<>();
    }

    // User is editing a group
    public Intent addGroupDataToIntent(Intent intent, int position) {
        intent.putExtra(GROUP_KEY, entryData.getDefinitionGroup(position));
        intent.putExtra(DEFINITION_CHANGES, definitionChangesMap.get(entryData.getDefinitionGroup(position).getType()));
        return intent;
    }

    // User is creating a new group
    public Intent addGroupDataToIntent(Intent intent, String type) {
        DefinitionChanges newDefinitionChanges = new DefinitionChanges();
        definitionChangesMap.put(type, newDefinitionChanges);

        intent.putExtra(GROUP_KEY, new DefinitionGroupModel(type, entryData.getDefinitionGroups().size()-1));
        intent.putExtra(DEFINITION_CHANGES, newDefinitionChanges);
        return intent;
    }

    public Intent addResultDataToIntent(Intent intent) {
        intent.putExtra(MyEntryViewModel.ENTRY_TEXT_KEY, entryData.getEntry());
        intent.putExtra(MyEntryViewModel.ENTRY_ID_KEY, entryData.getId());
        intent.putExtra(MyEntryViewModel.ENTRY_DELETE, shouldDelete);
        intent.putExtra(MyEntryViewModel.ENTRY_EDIT, isEdit);

        return intent;
    }

    public boolean entryHasType(String type) {
        return entryData.hasGroup(type.toLowerCase());
    }

    public LiveData<List<DefinitionGroupModel>> getGroups() {
        return mGroups;
    }

    public void removeGroup(int position) {
        for(int i = position+1; i < entryData.getDefinitionGroups().size(); i++) {
            DefinitionGroupModel g = entryData.getDefinitionGroups().get(i);
            groupChanges.putItemUpdated(g.getType(), g);
        }

        DefinitionGroupModel groupRemoved = entryData.removeGroup(position);
        groupChanges.removeItem(groupRemoved.getType(), groupRemoved);
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
                    groupChanges.removeItem(definitionGroup.getType(), definitionGroup);
                    shouldDelete = true;
                    mResult.setValue(REMOVE_GROUP);
                } else {
                    if (entryData.hasGroup(type)) {
                        entryData.replaceDefinitionGroup(type, definitionGroup);
                        mResult.setValue(EDIT_GROUP);
                    } else {
                        entryData.addDefinitionGroup(definitionGroup);
                        groupChanges.addItem(definitionGroup.getType(), definitionGroup);
                        mResult.setValue(ADD_GROUP);
                    }

                    if (result.getData().getParcelableExtra(EntryViewModel.DEFINITION_CHANGES) != null) {
                        if (result.getData().getParcelableExtra(EntryViewModel.DEFINITION_CHANGES) instanceof DefinitionChanges) {
                            DefinitionChanges newChanges = result.getData().getParcelableExtra(DEFINITION_CHANGES);
                            definitionChangesMap.put(type, newChanges);
                        }
                    }
                }
            }
        }
    }

    public String parseRetrieved(Intent intent) {
        String entry = intent.getStringExtra(MyEntryViewModel.ENTRY_TEXT_KEY);
        int userId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1);

        compositeDisposable.add(
                vocabyRepository.getEntryData(userId, entry)
                        .subscribe(entryData -> {
                                    this.entryData = entryData;
                                    initialGroups = new ArrayList<>(entryData.getDefinitionGroups());
                                    groupChanges.setEntryId(entryData.getId());
                                    mGroups.setValue(entryData.getDefinitionGroups());

                                    for (DefinitionGroupModel group : entryData.getDefinitionGroups()) {
                                        definitionChangesMap.put(group.getType(), new DefinitionChanges(group.getGroupId()));
                                    }

                                    if (entryData.getId() == -1) {
                                        isEdit = false;
                                    }
                                }, error -> {
                                    error.printStackTrace();
                                    Bugsnag.notify(error);
                                }
                        )
        );

        return entry;
    }

    private void checkForUpdatedItems() {
        if (!entryData.getDefinitionGroups().isEmpty()) {
            // Checking for starting items order changes
            if (entryData.getId() != -1) {
                // Only add to updatedItems if the entry had data initially.
                int j = 0;
                for (int i = 0; i < initialGroups.size(); i++) {
                    if (groupChanges.hasItemDeleted(initialGroups.get(i).getType())
                            || groupChanges.hasItemAdded(initialGroups.get(i).getType())
                            || groupChanges.hasItemUpdated(initialGroups.get(i).getType())) {
                        continue;
                    }

                    if (!entryData.getDefinitionGroups().get(j).getType().equals(initialGroups.get(i).getType())) {
                        groupChanges.putItemUpdated(initialGroups.get(i).getType(), initialGroups.get(i));
                    }

                    j++;
                }
            }
        }
    }

    private void fixItemOrdering() {
        // Setting order for added items
        for (int i = 0; i < entryData.getDefinitionGroups().size(); i++) {
            String key = entryData.getDefinitionGroups().get(i).getType();
            DefinitionGroupModel item = null;
            if (groupChanges.hasItemAdded(key)) {
                item = groupChanges.getAddedItem(key);
            }

            if (groupChanges.hasItemUpdated(key)) {
                item = groupChanges.getUpdatedItem(key);
            }

            if (item != null) {
                item.setOrder(i);
            }
        }
    }

    public void saveUserEntry() {
        checkForUpdatedItems();
        fixItemOrdering();
        int userId = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1);

        if (entryData.getDefinitionGroups().isEmpty()) {
            if (entryData.getId() == -1) {
                // New entry should have at least one group when saving
                mResult.setValue(EMPTY_ENTRY);
            } else {
                // Delete the existing entry
                compositeDisposable.add(
                        vocabyRepository.deleteUserEntry(entryData.getId(), entryData.getEntry())
                            .subscribe(() -> {
                                shouldDelete = true;
                                mResult.setValue(SAVE_ENTRY);
                            }, error -> {
                                Log.d("vocabydebug", error.getMessage());
                            })
                );
            }
        } else {
            // Save
            compositeDisposable.add(
                    vocabyRepository.insertOrUpdateEntry(userId, entryData.getEntry(), groupChanges, definitionChangesMap)
                            .subscribe((id) -> {
                                entryData.setId(id);
                                mResult.setValue(SAVE_ENTRY);
                            }, error -> {
                                Log.d("vocabydebug", error.getMessage());
                            })
            );
        }
    }
}
