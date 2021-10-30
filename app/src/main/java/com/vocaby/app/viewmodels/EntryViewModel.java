package com.vocaby.app.viewmodels;

import static com.vocaby.app.Constants.ITEM_PAYLOAD_KEY;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vocaby.app.data.VocabyRepository;
import com.vocaby.app.models.customentry.DefinitionChanges;
import com.vocaby.app.models.customentry.GroupChanges;
import com.vocaby.app.models.dictionary.DefinitionGroupModel;
import com.vocaby.app.models.dictionary.EntryModel;
import com.vocaby.app.models.payload.ItemIntPayload;
import com.vocaby.app.models.payload.ItemStringPayload;
import com.vocaby.app.models.payload.PayloadState;
import com.vocaby.app.utils.Logger;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class EntryViewModel extends AndroidViewModel {
    public static final String GROUP_KEY = "GK";
    public static final String INITIAL_DEFINITIONS_KEY = "IDK";
    public static final String DEFINITION_CHANGES = "DEFCK";

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;

    private EntryModel entryData;
    private final HashMap<String, DefinitionGroupModel> initialGroups;
    private ItemStringPayload receivedEntryPayload;
    private final GroupChanges groupChanges;
    private final Map<String, DefinitionChanges> definitionChangesMap;
    private int selectedGroup = -1;

    private final SingleLiveEvent<String> mEntry;
    private final SingleLiveEvent<String> mPronunciation;
    private final SingleLiveEvent<ItemIntPayload> mGroupChange;
    private final SingleLiveEvent<ItemStringPayload> mTypeChange;
    private final SingleLiveEvent<List<DefinitionGroupModel>> mGroups;
    private final SingleLiveEvent<Boolean> mSaveResult;
    private final SingleLiveEvent<List<String>> mTypes;
    private final SingleLiveEvent<String> mSelectedType;

    public EntryViewModel(Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();

        mEntry = new SingleLiveEvent<>();
        mPronunciation = new SingleLiveEvent<>();
        mGroups = new SingleLiveEvent<>();
        mGroupChange = new SingleLiveEvent<>();
        mSaveResult = new SingleLiveEvent<>();
        mTypes = new SingleLiveEvent<>();
        mSelectedType = new SingleLiveEvent<>();
        mTypeChange = new SingleLiveEvent<>();

        definitionChangesMap = new HashMap<>();
        groupChanges = new GroupChanges(-1);
        initialGroups = new HashMap<>();

        compositeDisposable.add(
                vocabyRepository.getAllTypes().subscribe(mTypes::setValue, Logger::reportError)
        );

        mSelectedType.setValue("");
    }

    public LiveData<String> getEntry() {
        return mEntry;
    }

    public LiveData<String> getPronunciation() {
        return mPronunciation;
    }

    public LiveData<List<DefinitionGroupModel>> getGroups() {
        return mGroups;
    }

    public LiveData<ItemIntPayload> getGroupChange() {
        return mGroupChange;
    }

    public LiveData<ItemStringPayload> getTypeChange() {
        return mTypeChange;
    }

    public LiveData<Boolean> getSaveResult() {
        return mSaveResult;
    }

    public LiveData<List<String>> getTypes() {
        return mTypes;
    }

    public LiveData<String> getSelectedType() {
        return mSelectedType;
    }

    public void parseRetrieved(Intent intent) {
        receivedEntryPayload = intent.getParcelableExtra(ITEM_PAYLOAD_KEY);

        compositeDisposable.add(
            vocabyRepository.getEntryData(receivedEntryPayload.getPayload())
                    .subscribe(entryData -> {
                                // SETUP
                                this.entryData = entryData;
                                mEntry.setValue(entryData.getEntry());
                                groupChanges.setEntryId(entryData.getId());
                                mGroups.setValue(entryData.getDefinitionGroups());
                                mPronunciation.setValue(entryData.getPronunciation());
                                List<String> types = mTypes.getValue();

                                for (DefinitionGroupModel group : entryData.getDefinitionGroups()) {
                                    DefinitionGroupModel clone = new DefinitionGroupModel(group);
                                    initialGroups.put(clone.getType(), clone);
                                    definitionChangesMap.put(clone.getType(), new DefinitionChanges(clone.getGroupId()));
                                    if (types != null) types.remove(clone.getType());
                                }

                                mTypes.setValue(types);

                                // In case user attempts to create a new entry but the entry already exists
                                if (!entryData.isEmpty()) receivedEntryPayload.setState(PayloadState.UPDATE);
                            }, Logger::reportError
                    )
        );
    }

    public Intent addExistingGroupDataToIntent(Intent intent, int position) {
        String type  = entryData.getDefinitionGroup(position).getType();
        intent.putExtra(GROUP_KEY, entryData.getDefinitionGroup(position));
        intent.putExtra(DEFINITION_CHANGES, definitionChangesMap.get(type));
        intent.putExtra(INITIAL_DEFINITIONS_KEY, initialGroups.get(type));
        intent.putExtra(ITEM_PAYLOAD_KEY, PayloadState.UPDATE);
        return intent;
    }

    public Intent addNewGroupDataToIntent(Intent intent, String type) {
        DefinitionGroupModel newGroup = new DefinitionGroupModel(type, entryData.getDefinitionGroups().size() - 1);
        intent.putExtra(GROUP_KEY, newGroup);
        intent.putExtra(DEFINITION_CHANGES, new DefinitionChanges());
        intent.putExtra(INITIAL_DEFINITIONS_KEY, newGroup);
        intent.putExtra(ITEM_PAYLOAD_KEY, PayloadState.ADD);
        return intent;
    }

    public void setSelectedGroup(int selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public void removeGroup(int groupDefinitionState, int position) {
        if (groupDefinitionState == PayloadState.UPDATE) {
            DefinitionGroupModel groupRemoved = entryData.removeGroup(position);
            // clear definition changes as well for the removed group
            definitionChangesMap.remove(groupRemoved.getType());
            groupChanges.removeItem(groupRemoved.getType(), groupRemoved);

            mGroupChange.setValue(new ItemIntPayload(PayloadState.DELETE, position));
            mTypeChange.setValue(new ItemStringPayload(PayloadState.ADD, groupRemoved.getType()));
        }
    }

    public void addGroup(DefinitionGroupModel newGroup) {
        entryData.addDefinitionGroup(newGroup);
        groupChanges.putItemAdded(newGroup.getType(), newGroup);
        mGroupChange.setValue(new ItemIntPayload(PayloadState.ADD, selectedGroup));
        mTypeChange.setValue(new ItemStringPayload(PayloadState.DELETE, newGroup.getType()));
    }

    public void handleGroupCreationResult(ActivityResult result) {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData().getParcelableExtra(GROUP_KEY) instanceof DefinitionGroupModel) {
                DefinitionGroupModel definitionGroup = result.getData().getParcelableExtra(GROUP_KEY);
                String type = definitionGroup.getType();
                int groupDefinitionState = result.getData().getIntExtra(ITEM_PAYLOAD_KEY, PayloadState.ADD);

                if (definitionGroup.isEmpty()) {
                    removeGroup(groupDefinitionState, selectedGroup);
                } else {
                    if (groupDefinitionState == PayloadState.UPDATE) {
                        entryData.replaceDefinitionGroup(type, definitionGroup);
                        groupChanges.putItemUpdated(definitionGroup.getType(), definitionGroup);
                        mGroupChange.setValue(new ItemIntPayload(PayloadState.UPDATE, selectedGroup));
                    } else {
                        addGroup(definitionGroup);
                    }

                    // add/overwrite the definition changes
                    DefinitionChanges newChanges = result.getData().getParcelableExtra(DEFINITION_CHANGES);
                    definitionChangesMap.put(type, newChanges);
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

    // USER CLICKS SAVE
    public void saveUserEntry(String pronunciation) {
        pronunciation = pronunciation.trim();
        checkForUpdatedItems();

        if (entryData.getDefinitionGroups().isEmpty()) {
            if (receivedEntryPayload.getState() == PayloadState.ADD) {
                // NEW ENTRY IS EMPTY SO CANCEL
                mSaveResult.setValue(false);
            } else {
                // DELETE THE EXISTING ENTRY BECAUSE THE USER DELETED ALL GROUPS
                compositeDisposable.add(
                        vocabyRepository.deleteUserEntry(entryData.getId(), entryData.getEntry())
                                .subscribe(() -> {
                                    receivedEntryPayload.setState(PayloadState.DELETE);
                                    mSaveResult.setValue(true);
                                }, Logger::reportError)
                );
            }
        } else {
            compositeDisposable.add(
                    vocabyRepository.insertOrUpdateEntry(
                            entryData.getEntry(),
                            pronunciation,
                            groupChanges,
                            definitionChangesMap
                    ).subscribe((id) -> {
                        entryData.setId(id);
                        mSaveResult.setValue(true);
                    }, Logger::reportError)
            );
        }
    }

    private void checkForUpdatedItems() {
        if (!entryData.getDefinitionGroups().isEmpty() && receivedEntryPayload.getState() == PayloadState.UPDATE) {
            for (int i = 0; i < entryData.getDefinitionGroups().size(); i++) {
                DefinitionGroupModel currentGroup = entryData.getDefinitionGroups().get(i);
                DefinitionGroupModel originalGroup = initialGroups.get(currentGroup.getType());

                if (originalGroup != null) {
                    if (originalGroup.getOrder() == currentGroup.getOrder()){
                        groupChanges.removeItemUpdated(currentGroup.getType());
                    } else {
                        groupChanges.putItemUpdated(currentGroup.getType(), currentGroup);
                    }
                }
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