package com.vocaby.app.models;

import com.google.gson.annotations.SerializedName;
import com.vocaby.app.data.entity.UserSaves;

import java.util.List;

public class UserSavesSyncModel {
    @SerializedName("offline_added")
    final List<UserSaves> wordsToAddLocally;

    @SerializedName("offline_removed")
    final List<String> wordsToRemoveLocally;

    public UserSavesSyncModel(List<UserSaves> wordsToAddLocally, List<String> wordsToRemoveLocally) {
        this.wordsToAddLocally = wordsToAddLocally;
        this.wordsToRemoveLocally = wordsToRemoveLocally;
    }

    public List<UserSaves> getWordsToAddLocally() {
        return wordsToAddLocally;
    }

    public List<String> getWordsToRemoveLocally() {
        return wordsToRemoveLocally;
    }
}
