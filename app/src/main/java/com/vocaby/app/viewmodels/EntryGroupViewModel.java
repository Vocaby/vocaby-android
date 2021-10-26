package com.vocaby.app.viewmodels;

import static com.vocaby.app.Constants.ITEM_PAYLOAD_KEY;

import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.RawQuery;

import com.vocaby.app.models.DefinitionChanges;
import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.models.ItemState;
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
    private Set<String> initialAddedDataSet;
    private int resultState;

    public EntryGroupViewModel() {
        definitionChanges = new DefinitionChanges();
        initialDefinitions = new ArrayList<>();
        initialAddedDataSet = new HashSet<>();

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
            initialDefinitions = new ArrayList<>(definitionGroup.getDefinitionData());
            mType.setValue(definitionGroup.getType());
        }

        if (receivedIntent.getParcelableExtra(EntryViewModel.DEFINITION_CHANGES) != null) {
            definitionChanges = receivedIntent.getParcelableExtra(EntryViewModel.DEFINITION_CHANGES);
            initialAddedDataSet = definitionChanges.getAddedKeySet();
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

        intent.putExtra(ITEM_PAYLOAD_KEY, resultState);
        intent.putExtra(EntryViewModel.DEFINITION_CHANGES, definitionChanges);
        intent.putExtra(EntryViewModel.GROUP_KEY, definitionGroup);

        return intent;
    }

    private void checkForUpdatedItems() {
        if (!definitionGroup.getDefinitionData().isEmpty()) {
            // Checking for starting items order changes
            int j = 0;
            for (int i = 0; i < initialDefinitions.size() && j < definitionGroup.getDefinitionData().size(); i++) {
                if (initialAddedDataSet.contains(initialDefinitions.get(i).getDefinition())) {
                    Log.d("vocabydebug", "1: " + i + " > " + initialDefinitions.get(i).getDefinition());
                    Log.d("vocabydebug", "1: " + j + " > " + definitionGroup.getDefinitionData().get(j));
                    continue;
                }

                // item should be marked as updated if its top neighbour was deleted
                // should i persist the original definitions on entryviewmodel init?
                //
                if (i != j && definitionGroup.getDefinitionData().get(j).getDefinition().equals(initialDefinitions.get(i).getDefinition())) {
                    Log.d("vocabydebug", "2: " + i + " > " + initialDefinitions.get(i).getDefinition());
                    Log.d("vocabydebug", "2: " + j + " > " + definitionGroup.getDefinitionData().get(j));
                    definitionChanges.putItemUpdated(definitionGroup.getDefinitionData().get(j).getDefinition(), definitionGroup.getDefinitionData().get(j));
                }

                // adding swapped items
                if (i == j && !definitionGroup.getDefinitionData().get(j).getDefinition().equals(initialDefinitions.get(i).getDefinition())) {
                    Log.d("vocabydebug", "3: " + i + " > " + initialDefinitions.get(i).getDefinition());
                    Log.d("vocabydebug", "3: " + j + " > " + definitionGroup.getDefinitionData().get(j));
                    definitionChanges.putItemUpdated(definitionGroup.getDefinitionData().get(j).getDefinition(), definitionGroup.getDefinitionData().get(j));
                }

                j++;
            }
        }
    }
}
