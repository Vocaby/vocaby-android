package com.vocaby.app.viewmodels;

import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.vocaby.app.models.DefinitionGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

public class EntryGroupViewModel extends ViewModel {

    private DefinitionGroupModel definitionGroup;
    private final SingleLiveEvent<String> mType;
    private final SingleLiveEvent<List<DefinitionModel>> mDefinitions;
    private boolean isEdit;

    public EntryGroupViewModel() {
        mDefinitions = new SingleLiveEvent<>();
        mType = new SingleLiveEvent<>();
        isEdit = false;
    }

    public void addDefinition(String definition, String type, String example) {
        definitionGroup.addDefinition(definition, example);
        mDefinitions.setValue(definitionGroup.getDefinitionData());
    }

    public LiveData<List<DefinitionModel>> getDefinitions() {
        return mDefinitions;
    }

    public void removeDefinition(int position) {
        definitionGroup.removeDefinition(position);
        mDefinitions.setValue(definitionGroup.getDefinitionData());
    }

    public Intent addSaveDataToIntent(Intent intent) {
        intent.putExtra(EntryViewModel.GROUP_KEY, definitionGroup);
        return intent;
    }

    public List<DefinitionModel> getCurrentData() {
        return mDefinitions.getValue();
    }

    public LiveData<String> getType() {
        return mType;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void handleIntent(Intent receivedIntent) {
        if (receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY) != null) {
            if (receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY) instanceof DefinitionGroupModel) {
                definitionGroup = receivedIntent.getParcelableExtra(EntryViewModel.GROUP_KEY);
                mDefinitions.setValue(definitionGroup.getDefinitionData());
                mType.setValue(definitionGroup.getType());
            }

            if (!definitionGroup.isEmpty()) {
                isEdit = true;
            }
        }
    }
}
