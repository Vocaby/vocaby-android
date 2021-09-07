package com.vocaby.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OfflineDataModel {
    @SerializedName("offline_added")
    final List<String> offlineAdded;

    @SerializedName("offline_removed")
    final List<String> offlineRemoved;

    public OfflineDataModel(List<String> offlineAdded, List<String> offlineRemoved) {
        this.offlineAdded = offlineAdded;
        this.offlineRemoved = offlineRemoved;
    }

    public List<String> getOfflineAdded() {
        return this.offlineAdded;
    }

    public List<String> getOfflineRemoved() {
        return this.offlineRemoved;
    }

    public int getOfflineAddedCount() {
        return this.offlineAdded.size();
    }

    public int getOfflineRemovedCount() {
        return this.offlineRemoved.size();
    }
}
