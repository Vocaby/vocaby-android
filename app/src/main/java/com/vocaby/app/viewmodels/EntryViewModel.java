package com.vocaby.app.viewmodels;

import android.app.Activity;

import androidx.activity.result.ActivityResult;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.vocaby.app.models.CustomEntryGroupModel;
import com.vocaby.app.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntryViewModel extends ViewModel {
    public static final int ADD_GROUP = 0;
    public static final int EDIT_GROUP = 1;
    public static final int REMOVE_GROUP = 2;

    private List<CustomEntryGroupModel> currentGroups;
    private SingleLiveEvent<Integer> mResult;
    private SingleLiveEvent<List<CustomEntryGroupModel>> mGroups;
    private int selectedItemPosition;

    public EntryViewModel() {
        selectedItemPosition = 0;
        currentGroups = new ArrayList<>();
        mGroups = new SingleLiveEvent<>();
        mResult = new SingleLiveEvent<>();
        mGroups.setValue(currentGroups);
    }

    public void addGroup(CustomEntryGroupModel newGroup) {
        currentGroups.add(newGroup);
        mGroups.setValue(currentGroups);
    }

    public Set<String> getGroupTypes() {
        Set<String> types = new HashSet<>();
        for (CustomEntryGroupModel group : currentGroups) {
            types.add(group.getType());
        }

        return types;
    }

    private void editGroup(CustomEntryGroupModel newGroup, int position) {
        currentGroups.set(position, newGroup);
    }

    public LiveData<List<CustomEntryGroupModel>> getGroups() {
        return mGroups;
    }

    public void removeGroup(int position) {
        currentGroups.remove(position);
        mGroups.setValue(currentGroups);
    }

    public List<CustomEntryGroupModel> getCurrentData() {
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
            if (result.getData().getParcelableExtra("groupData") instanceof CustomEntryGroupModel) {
                CustomEntryGroupModel customEntryGroupModel = result.getData().getParcelableExtra("groupData");
                if(customEntryGroupModel.isEmpty()) {
                    mResult.setValue(REMOVE_GROUP);
                    removeGroup(selectedItemPosition);
                } else {
                    boolean add = true;
                    for (int i = 0; i < currentGroups.size(); i++) {
                        if (currentGroups.get(i).getType().equals(customEntryGroupModel.getType())) {
                            mResult.setValue(EDIT_GROUP);
                            editGroup(customEntryGroupModel, i);
                            add = false;
                        }
                    }

                    if (add) {
                        addGroup(customEntryGroupModel);
                        mResult.setValue(ADD_GROUP);
                    }
                }
            }
        }
    }
}
