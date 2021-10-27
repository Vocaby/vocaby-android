package com.vocaby.app.viewmodels;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.vocaby.app.models.DefinitionChanges;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.models.ItemState;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import static com.vocaby.app.Constants.ITEM_PAYLOAD_KEY;

public class EntryGroupViewModel extends ViewModel {
    private DefinitionGroupModel definitionGroup;
    private DefinitionChanges definitionChanges;
    private List<DefinitionModel> initialDefinitions;
    private int resultState;

    private final SingleLiveEvent<String> mType;
    private final SingleLiveEvent<List<DefinitionModel>> mDefinitions;

    public EntryGroupViewModel() {
        definitionChanges = new DefinitionChanges();
        initialDefinitions = new ArrayList<>();

        mDefinitions = new SingleLiveEvent<>();
        mType = new SingleLiveEvent<>();

        resultState = ItemState.ADD;
    }

    public LiveData<List<DefinitionModel>> getDefinitions() {
        return mDefinitions;
    }
    public LiveData<String> getType() {
        return mType;
    }

    public void handleIntent(Intent receivedIntent) {
        if (receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY) != null) {
            definitionGroup = receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY);
            mDefinitions.setValue(definitionGroup.getDefinitionData());
            mType.setValue(definitionGroup.getType());
        }

        if (receivedIntent.getParcelableExtra(EntryViewModel.INITIAL_DEFINITIONS_KEY) != null) {
            DefinitionGroupModel initGroup =
                    receivedIntent.getParcelableExtra(EntryViewModel.INITIAL_DEFINITIONS_KEY);
            initialDefinitions = initGroup.getDefinitionData();
        }

        if (receivedIntent.getParcelableExtra(EntryViewModel.DEFINITION_CHANGES) != null) {
            // holds a copy, not a reference. So swapping items will not affect the entry here.
            definitionChanges = receivedIntent.getParcelableExtra(EntryViewModel.DEFINITION_CHANGES);
        }

        resultState = receivedIntent.getIntExtra(ITEM_PAYLOAD_KEY, ItemState.UNCHANGED);
    }

    public void addDefinition(String definition, String example) {
        DefinitionModel definitionAdded = definitionGroup.addDefinition(definition, example);
        definitionChanges.addItem(definition, definitionAdded);
    }

    public void removeDefinition(int position) {
        // Remove the definition
        DefinitionModel definitionRemoved = definitionGroup.removeDefinition(position);

        // Add the removed definition to the changes model
        definitionChanges.removeItem(definitionRemoved.getDefinition(), definitionRemoved);
    }

    public Intent addSaveDataToIntent(Intent intent) {
        checkForUpdatedItems();
        fixOrdering();

        intent.putExtra(ITEM_PAYLOAD_KEY, resultState);
        intent.putExtra(EntryViewModel.DEFINITION_CHANGES, definitionChanges);
        intent.putExtra(EntryViewModel.GROUP_KEY, definitionGroup);

        return intent;
    }

    private void checkForUpdatedItems() {
        if (!definitionGroup.getDefinitionData().isEmpty() && !initialDefinitions.isEmpty()) {
            int j = 0;
            for (int i = 0; i < definitionGroup.getDefinitionData().size(); i++) {
                DefinitionModel originalDefinition = initialDefinitions.get(j);
                DefinitionModel currentDefinition = definitionGroup.getDefinitionData().get(i);

                if (i != j & currentDefinition.getDefinition().equals(originalDefinition.getDefinition())) {
                    definitionChanges.putItemUpdated(currentDefinition.getDefinition(), currentDefinition);
                } else if (i == j & !currentDefinition.getDefinition().equals(originalDefinition.getDefinition())) {
                    definitionChanges.putItemUpdated(currentDefinition.getDefinition(), currentDefinition);
                } else {
                    definitionChanges.removeItemUpdated(currentDefinition.getDefinition());
                }

                if (j != initialDefinitions.size()-1) j++;
            }
        }
    }

    // definitionChanges map does not hold references of definitionGroup.definitions when
    // the user updates a newly created group. this is a problem because the setOrder
    // will not be reflected in definition changes. this method will fix that
    private void fixOrdering() {
        // if it's a new group that's being updated
        if (definitionGroup.getGroupId() == -1 && resultState == ItemState.UPDATE) {
            for (DefinitionModel def : definitionGroup.getDefinitionData()) {
                if (definitionChanges.hasItemAdded(def.getDefinition())) {
                    definitionChanges.putItemAdded(def.getDefinition(), def);
                }
            }
        }
    }
}
