package com.vocaby.app.viewmodels;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.vocaby.app.models.DefinitionChanges;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntryGroupViewModel extends ViewModel {
    private DefinitionGroupModel definitionGroup;
    private DefinitionChanges definitionChanges;

    private final SingleLiveEvent<String> mType;
    private final SingleLiveEvent<List<DefinitionModel>> mDefinitions;

    private List<DefinitionModel> initialDefinitions;
    private Set<String> initialDataSet;

    public EntryGroupViewModel() {
        definitionChanges = new DefinitionChanges();
        initialDefinitions = new ArrayList<>();
        initialDataSet = new HashSet<>();

        mDefinitions = new SingleLiveEvent<>();
        mType = new SingleLiveEvent<>();
    }

    public LiveData<List<DefinitionModel>> getDefinitions() {
        return mDefinitions;
    }
    public LiveData<String> getType() {
        return mType;
    }

    public void handleIntent(Intent receivedIntent) {
        if (receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY) != null) {
            if (receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY) instanceof DefinitionGroupModel) {
                definitionGroup = receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY);
                mDefinitions.setValue(definitionGroup.getDefinitionData());
                initialDefinitions = new ArrayList<>(definitionGroup.getDefinitionData());
                mType.setValue(definitionGroup.getType());
            }
        }

        if (receivedIntent.getParcelableExtra(EntryViewModel.DEFINITION_CHANGES) != null) {
            if (receivedIntent.getParcelableExtra(EntryViewModel.DEFINITION_CHANGES) instanceof DefinitionChanges) {
                definitionChanges = receivedIntent.getParcelableExtra(EntryViewModel.DEFINITION_CHANGES);
                initialDataSet = definitionChanges.getAddedKeySet();
            }
        }
    }

    public void addDefinition(String definition, String example) {
        DefinitionModel definitionAdded = definitionGroup.addDefinition(definition, example);
        definitionChanges.addItem(definition, definitionAdded);
    }

    public void removeDefinition(int position) {
        // Definitions have been offset by one so they should be marked as updated items
        for(int i = position+1; i < definitionGroup.getDefinitionData().size(); i++) {
            DefinitionModel d = definitionGroup.getDefinitionData().get(i);
            definitionChanges.putItemUpdated(d.getDefinition(), d);
        }

        // Remove the definition
        DefinitionModel definitionRemoved = definitionGroup.removeDefinition(position);
        // Add the removed definition to the changes model
        definitionChanges.removeItem(definitionRemoved.getDefinition(), definitionRemoved);
    }

    public Intent addSaveDataToIntent(Intent intent) {
        checkForUpdatedItems();
        fixItemOrdering();

        intent.putExtra(EntryViewModel.GROUP_KEY, definitionGroup);
        intent.putExtra(EntryViewModel.DEFINITION_CHANGES, definitionChanges);

        return intent;
    }

    private void checkForUpdatedItems() {
        if (!definitionGroup.getDefinitionData().isEmpty()) {
            // Checking for starting items order changes
            int j = 0;
            for (int i = 0; i < initialDefinitions.size(); i++) {
                if (definitionChanges.hasItemDeleted(initialDefinitions.get(i).getDefinition())
                        || initialDataSet.contains(initialDefinitions.get(i).getDefinition())
                        || definitionChanges.hasItemUpdated(initialDefinitions.get(i).getDefinition())) {
                    continue;
                }

                if (!definitionGroup.getDefinitionData().get(j).getDefinition().equals(initialDefinitions.get(i).getDefinition())) {
                    definitionChanges.putItemUpdated(initialDefinitions.get(i).getDefinition(), initialDefinitions.get(i));
                }

                j++;
            }
        }
    }

    private void fixItemOrdering() {
        for (int i = 0; i < definitionGroup.getDefinitionData().size(); i++) {
            String key = definitionGroup.getDefinitionData().get(i).getDefinition();
            DefinitionModel item = null;
            if (definitionChanges.hasItemAdded(key)) {
                item = definitionChanges.getAddedItem(key);
            }

            if (definitionChanges.hasItemUpdated(key)) {
                item = definitionChanges.getUpdatedItem(key);
            }

            if (item != null) {
                item.setOrder(i);
            }
        }
    }
}
