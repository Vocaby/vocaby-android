package com.vocaby.app.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.vocaby.app.models.CustomEntryGroupModel;
import com.vocaby.app.models.DefinitionModel;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

public class EntryGroupViewModel extends ViewModel {

    private List<DefinitionModel> definitions;
    private final SingleLiveEvent<String> mType;
    private final SingleLiveEvent<List<DefinitionModel>> mDefinitions;
    private boolean isEdit;

    public EntryGroupViewModel() {
        definitions = new ArrayList<>();
        mDefinitions = new SingleLiveEvent<>();
        mType = new SingleLiveEvent<>();
        mDefinitions.setValue(definitions);
        isEdit = false;
    }

    public void addDefinition(String definition, String sentence) {
        definitions.add(new DefinitionModel(definition, sentence));
        mDefinitions.setValue(definitions);
    }

    public LiveData<List<DefinitionModel>> getDefinitions() {
        return mDefinitions;
    }

    public void removeDefinition(int position) {
        definitions.remove(position);
        mDefinitions.setValue(definitions);
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
        if (receivedIntent.getStringExtra("type") != null) {
            String type = receivedIntent.getStringExtra("type");
            mType.setValue(type);
        } else if (receivedIntent.getParcelableExtra("definitionData") != null) {
            if (receivedIntent.getParcelableExtra("definitionData") instanceof CustomEntryGroupModel) {
                CustomEntryGroupModel customEntryGroupModel = receivedIntent.getParcelableExtra("definitionData");
                definitions = customEntryGroupModel.getDefinitionData();
                mDefinitions.setValue(definitions);
                mType.setValue(customEntryGroupModel.getType());
            }
        }

        isEdit = receivedIntent.getBooleanExtra("edit", false);
    }
}
