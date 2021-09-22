package com.vocaby.app.viewmodels;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bugsnag.android.Bugsnag;
import com.vocaby.app.data.entity.CustomDefinition;
import com.vocaby.app.data.entity.CustomEntryGroup;
import com.vocaby.app.data.entity.CustomExample;
import com.vocaby.app.data.entity.EntryDefinitionWithExamples;
import com.vocaby.app.data.entity.EntryGroupWithDefinitions;
import com.vocaby.app.data.entity.EntryWithDefinitions;
import com.vocaby.app.data.entity.Type;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.repositories.VocabyRepository;
import com.vocaby.app.utils.SingleLiveEvent;
import com.vocaby.app.utils.StringFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class EntryViewModel extends AndroidViewModel {
    public static final int ADD_GROUP = 0;
    public static final int EDIT_GROUP = 1;
    public static final int REMOVE_GROUP = 2;
    public static final int CREATE_ENTRY = 3;
    public static final int EMPTY_ENTRY = 4;

    private final VocabyRepository vocabyRepository;
    private final CompositeDisposable compositeDisposable;
    private String currentEntry;

    private List<DefinitionGroupModel> currentGroups;
    private final SingleLiveEvent<Integer> mResult;
    private final SingleLiveEvent<List<DefinitionGroupModel>> mGroups;
    private final SingleLiveEvent<List<Type>> mTypes;
    private final SingleLiveEvent<Integer> mEntryId;

    private final SharedPreferences userSharedPreference;

    private int selectedItemPosition;

    public EntryViewModel(Application application) {
        super(application);
        vocabyRepository = new VocabyRepository(application);
        compositeDisposable = new CompositeDisposable();
        userSharedPreference = getApplication().getSharedPreferences("USER_ID", Context.MODE_PRIVATE);
        currentEntry = "";

        selectedItemPosition = 0;
        mGroups = new SingleLiveEvent<>();
        currentGroups = new ArrayList<>();
        mTypes = new SingleLiveEvent<>();
        mEntryId = new SingleLiveEvent<>();

        compositeDisposable.add(
            vocabyRepository.getTypes()
                    .subscribe(mTypes::setValue)
        );

        // Used to notify adapter
        mResult = new SingleLiveEvent<>();
    }

    public void getEntry(int entryId) {
        compositeDisposable.add(
                vocabyRepository.getEntryData(entryId)
                        .subscribe(entryData -> {
                                currentGroups = convertEntryData(entryData);
                                mGroups.setValue(currentGroups);
                            }, error -> {
                                error.printStackTrace();
                                Bugsnag.notify(error);
                            }
                        )
        );
    }


    private List<DefinitionGroupModel> convertEntryData(EntryWithDefinitions entry) {
        List<DefinitionGroupModel> data = new ArrayList<>();
        entry.groups.sort(Comparator.naturalOrder());

        for (EntryGroupWithDefinitions group : entry.groups) {
            String type = group.type.getType();
            ArrayList<DefinitionModel> definitionModels = new ArrayList<>();
            group.definitions.sort(Comparator.naturalOrder());

            for (EntryDefinitionWithExamples definitionWithExamples : group.definitions) {
                String definition = definitionWithExamples.customDefinition.getDefinition();
                List<String> examples = definitionWithExamples.examples.stream()
                        .map( Object::toString )
                        .collect(Collectors.toList() );
                definitionModels.add(new DefinitionModel(definition, type, examples));
            }

            data.add(new DefinitionGroupModel(type, definitionModels));
        }

        return data;
    }

    public void addGroup(DefinitionGroupModel newGroup) {
        currentGroups.add(newGroup);
        mGroups.setValue(currentGroups);
    }

    public Set<String> getGroupTypes() {
        Set<String> types = new HashSet<>();
        if (mGroups.getValue() != null) {
            for (DefinitionGroupModel group : currentGroups) {
                types.add(group.getType());
            }
        }

        return types;
    }

    public LiveData<List<Type>> getTypes() {
        return mTypes;
    }

    public LiveData<Integer> getEntryId() {
        return mEntryId;
    }

    public void setEntry(String entry) {
        currentEntry = StringFormatter.cleanText(entry);
    }

    private void editGroup(DefinitionGroupModel newGroup, int position) {
        currentGroups.set(position, newGroup);
    }

    public LiveData<List<DefinitionGroupModel>> getGroups() {
        return mGroups;
    }

    public void removeGroup(int position) {
        currentGroups.remove(position);
        mGroups.setValue(currentGroups);
    }

    public List<DefinitionGroupModel> getCurrentData() {
        return mGroups.getValue();
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
            if (result.getData().getParcelableExtra("groupData") instanceof DefinitionGroupModel) {
                DefinitionGroupModel definitionGroup = result.getData().getParcelableExtra("groupData");
                if(definitionGroup.isEmpty()) {
                    // Todo: Remove group from the local database
                    mResult.setValue(REMOVE_GROUP);
                } else {
                    boolean add = true;

                    // Check if the type exists
                    for (int i = 0; i < currentGroups.size(); i++) {
                        if (currentGroups.get(i).getType().equals(definitionGroup.getType())) {
                            mResult.setValue(EDIT_GROUP);
                            editGroup(definitionGroup, i);
                            add = false;
                        }
                    }

                    // Type doesn't exist so add the new group.
                    if (add) {
                        addGroup(definitionGroup);
                        mResult.setValue(ADD_GROUP);
                    }
                }
            }
        }
    }

    public void createNewEntry() {
        if (currentGroups.isEmpty()) {
            mResult.setValue(EMPTY_ENTRY);
        } else {
            int currentId = userSharedPreference.getInt("CURRENT_USER_ID", 1);
            AtomicInteger entryId = new AtomicInteger(1);

            compositeDisposable.add(
                    vocabyRepository.insertCustomEntry(currentId, currentEntry, System.currentTimeMillis())
                            .flatMap(id -> {
                                entryId.set(id.intValue());
                                List<CustomEntryGroup> entryGroups = new ArrayList<>();
                                if (mTypes.getValue() != null) {
                                    for (int i = 0; i < currentGroups.size(); i++) {
                                        int typeId = 0;

                                        // Search for the correct type_id
                                        if (mTypes.getValue() != null) {
                                            for (Type type : mTypes.getValue()) {
                                                if (currentGroups.get(i).getType().equals(type.getType())) {
                                                    typeId = type.getId();
                                                }
                                            }
                                        }

                                        CustomEntryGroup group = new CustomEntryGroup(entryId.get(), typeId, i);
                                        entryGroups.add(group);
                                    }
                                }

                                return vocabyRepository.insertCustomEntryGroups(entryGroups);

                            }).flatMap(ids -> {
                        List<CustomDefinition> definitions = new ArrayList<>();
                        for (int i = 0; i < ids.size(); i++) {
                            int groupId = ids.get(i).intValue();
                            List<DefinitionModel> d = currentGroups.get(i).getDefinitionData();
                            for (int j = 0; j < d.size(); j++) {
                                CustomDefinition definition = new CustomDefinition(groupId, d.get(j).toString(), j);
                                definitions.add(definition);
                            }
                        }

                        return vocabyRepository.insertCustomDefinitions(definitions);
                    }).flatMapCompletable(defIds -> {
                        List<CustomExample> examples = new ArrayList<>();
                        int index = 0;
                        for (DefinitionGroupModel group : currentGroups) {
                            for (DefinitionModel definition : group.getDefinitionData()) {
                                // add examples to the current definition if there is any
                                for (String example : definition.getExamples()) {
                                    examples.add(new CustomExample(defIds.get(index).intValue(), example));
                                }
                                index++;
                            }
                        }

                        return vocabyRepository.insertCustomExamples(examples);
                    }).subscribe(() -> {
                        mEntryId.setValue(entryId.get());
                        mResult.setValue(CREATE_ENTRY);
                    }, error -> {
                        // TODO: Handle Unique Constraint Fails
                        Log.d("Vocabydebug", error.getMessage());
                    })
            );
        }
    }
}
