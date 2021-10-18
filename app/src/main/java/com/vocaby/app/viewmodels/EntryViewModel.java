package com.vocaby.app.viewmodels;

import static com.vocaby.app.Constants.ITEM_PAYLOAD_KEY;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vocaby.app.models.DefinitionChanges;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.EntryModel;
import com.vocaby.app.models.GroupChanges;
import com.vocaby.app.models.ItemIntPayload;
import com.vocaby.app.models.ItemPayload;
import com.vocaby.app.models.ItemStringPayload;
import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class EntryViewModel extends AndroidViewModel {
    public static final String GROUP_KEY = "GROUP_KEY";
    public static final String DEFINITION_CHANGES = "DEF_CHNGS";

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private EntryModel entryData;
    private List<DefinitionGroupModel> initialGroups;

    private final SingleLiveEvent<String> mEntry;
    private final SingleLiveEvent<ItemIntPayload> mGroupChange;
    private final SingleLiveEvent<ItemStringPayload> mTypeChange;
    private final SingleLiveEvent<List<DefinitionGroupModel>> mGroups;
    private final SingleLiveEvent<Boolean> mSaveResult;
    private final SingleLiveEvent<List<String>> mTypes;
    private final SingleLiveEvent<String> mSelectedType;

    private ItemStringPayload receivedEntryPayload;
    private final GroupChanges groupChanges;
    private final Map<String, DefinitionChanges> definitionChangesMap;
    private int selectedGroup = -1;

    public EntryViewModel(Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();

        mEntry = new SingleLiveEvent<>();
        mGroups = new SingleLiveEvent<>();
        mGroupChange = new SingleLiveEvent<>();
        mSaveResult = new SingleLiveEvent<>();
        mTypes = new SingleLiveEvent<>();
        mSelectedType = new SingleLiveEvent<>();
        mTypeChange = new SingleLiveEvent<>();

        definitionChangesMap = new HashMap<>();
        groupChanges = new GroupChanges(-1);
        initialGroups = new ArrayList<>();

        compositeDisposable.add(
            vocabyRepository.getAllTypes().subscribe(mTypes::setValue, Logger::reportError)
        );

        mSelectedType.setValue("");
    }

    public LiveData<String> getEntry() {
        return mEntry;
    }
    public LiveData<List<DefinitionGroupModel>> getGroups() { return mGroups; }
    public LiveData<ItemIntPayload> getGroupChange() { return mGroupChange; }
    public LiveData<ItemStringPayload> getTypeChange() { return mTypeChange; }
    public LiveData<Boolean> getSaveResult() { return mSaveResult; }
    public LiveData<List<String>> getTypes() { return mTypes; }
    public LiveData<String> getSelectedType() { return mSelectedType; }

    public void parseRetrieved(Intent intent) {
        receivedEntryPayload = intent.getParcelableExtra(ITEM_PAYLOAD_KEY);

        compositeDisposable.add(
            vocabyRepository.getCurrentUserId()
                .flatMap(userId -> vocabyRepository.getEntryData(userId, receivedEntryPayload.getPayload()))
                .subscribe(entryData -> {
                            this.entryData = entryData;
                            mEntry.setValue(entryData.getEntry());
                            initialGroups = new ArrayList<>(entryData.getDefinitionGroups());
                            groupChanges.setEntryId(entryData.getId());
                            mGroups.setValue(entryData.getDefinitionGroups());

                            List<String> types = mTypes.getValue();
                            for (DefinitionGroupModel group : initialGroups) {
                                definitionChangesMap.put(group.getType(), new DefinitionChanges(group.getGroupId()));
                                if (types != null) types.remove(group.getType());
                            }

                            mTypes.setValue(types);
                        }, Logger::reportError
                )
        );
    }

    // User is editing a group
    public Intent addGroupDataToIntent(Intent intent, int position) {
        intent.putExtra(GROUP_KEY, entryData.getDefinitionGroup(position));
        intent.putExtra(DEFINITION_CHANGES, definitionChangesMap.get(entryData.getDefinitionGroup(position).getType()));
        return intent;
    }

    // User is creating a new group
    public Intent addGroupDataToIntent(Intent intent, String type) {
        // New group requires a new definition changes object in the map.
        DefinitionChanges newDefinitionChanges = new DefinitionChanges();
        definitionChangesMap.put(type, newDefinitionChanges);

        intent.putExtra(GROUP_KEY, new DefinitionGroupModel(type, entryData.getDefinitionGroups().size()-1));
        intent.putExtra(DEFINITION_CHANGES, newDefinitionChanges);
        return intent;
    }

    public void setSelectedGroup(int selectedGroup) { this.selectedGroup = selectedGroup; }

    public void removeGroup(int position) {
        for(int i = position+1; i < entryData.getDefinitionGroups().size(); i++) {
            DefinitionGroupModel g = entryData.getDefinitionGroups().get(i);
            groupChanges.putItemUpdated(g.getType(), g);
        }

        DefinitionGroupModel groupRemoved = entryData.removeGroup(position);
        if (receivedEntryPayload.getState() == ItemPayload.NEW) {
            groupChanges.addItem(groupRemoved.getType(), groupRemoved);
        } else {
            groupChanges.putItemDeleted(groupRemoved.getType(), groupRemoved);
        }

        mTypeChange.setValue(new ItemStringPayload(ItemPayload.ADD, groupRemoved.getType()));
    }

    // BUG: Delete existing group and add the same group again
    public void handleResult(ActivityResult result) {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData().getParcelableExtra(GROUP_KEY) instanceof DefinitionGroupModel) {
                DefinitionGroupModel definitionGroup = result.getData().getParcelableExtra(GROUP_KEY);
                String type = definitionGroup.getType();

                if (definitionGroup.isEmpty()) {
                    if (selectedGroup != -1) entryData.removeGroup(selectedGroup);
                    if (receivedEntryPayload.getState() == ItemPayload.NEW) {
                        groupChanges.removeItem(definitionGroup.getType(), definitionGroup);
                    } else {
                        groupChanges.putItemDeleted(definitionGroup.getType(), definitionGroup);
                    }

                    mGroupChange.setValue(new ItemIntPayload(ItemPayload.DELETE, selectedGroup));
                    mTypeChange.setValue(new ItemStringPayload(ItemPayload.ADD, type));
                } else {
                    if (entryData.hasGroup(type)) {
                        entryData.replaceDefinitionGroup(type, definitionGroup);
                        mGroupChange.setValue(new ItemIntPayload(ItemPayload.UPDATE, selectedGroup));
                    } else {
                        if (receivedEntryPayload.getState() == ItemPayload.NEW) {
                            groupChanges.addItem(definitionGroup.getType(), definitionGroup);
                        } else {
                            groupChanges.putItemAdded(definitionGroup.getType(), definitionGroup);
                        }
                        entryData.addDefinitionGroup(definitionGroup);

                        mGroupChange.setValue(new ItemIntPayload(ItemPayload.ADD, selectedGroup));
                        mTypeChange.setValue(new ItemStringPayload(ItemPayload.DELETE, type));
                    }

                    // Add the definition changes
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

    // User saved the entry
    public Intent addEntryResultDataToIntent() {
        Intent intent = new Intent();
        intent.putExtra(ITEM_PAYLOAD_KEY, receivedEntryPayload);

        return intent;
    }

    public void saveUserEntry() {
        checkForUpdatedItems();
        fixItemOrdering();
        if (entryData.getDefinitionGroups().isEmpty()) {
            if (receivedEntryPayload.getState() == ItemPayload.NEW) {
                mSaveResult.setValue(false);
            } else {
                // Delete the existing entry
                compositeDisposable.add(
                        vocabyRepository.deleteUserEntry(entryData.getId(), entryData.getEntry())
                            .subscribe(() -> {
                                receivedEntryPayload.setState(ItemPayload.DELETE);
                                mSaveResult.setValue(true);
                            }, Logger::reportError)
                );
            }
        } else {
            if (!groupChanges.hasChanges()) {
                mSaveResult.setValue(false);
            } else {
                compositeDisposable.add(
                        vocabyRepository.getCurrentUserId()
                                .flatMap(userId -> vocabyRepository.insertOrUpdateEntry(userId, entryData.getEntry(), groupChanges, definitionChangesMap))
                                .subscribe((id) -> {
                                    entryData.setId(id);
                                    mSaveResult.setValue(true);
                                }, Logger::reportError)
                );
            }
        }
    }

    private void checkForUpdatedItems() {
        if (!entryData.getDefinitionGroups().isEmpty()) {
            // Checking for starting items order changes
            if (receivedEntryPayload.getState() == ItemPayload.UPDATE) {
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

    public void setSelectedType(String type) {
        mSelectedType.setValue(type);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}