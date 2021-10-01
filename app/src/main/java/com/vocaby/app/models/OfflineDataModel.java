package com.vocaby.app.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OfflineDataModel<T> {
    @SerializedName("offline_added")
    final List<T> offlineAdded;

    @SerializedName("offline_removed")
    final List<T> offlineRemoved;

    @SerializedName("offline_updated")
    final List<T> offlineUpdated;

    public OfflineDataModel(List<T> offlineAdded, List<T> offlineRemoved) {
        this.offlineAdded = offlineAdded;
        this.offlineRemoved = offlineRemoved;
        this.offlineUpdated = new ArrayList<>();
    }

    public OfflineDataModel(List<T> offlineAdded, List<T> offlineRemoved, List<T> offlineUpdated) {
        this.offlineAdded = offlineAdded;
        this.offlineRemoved = offlineRemoved;
        this.offlineUpdated = offlineUpdated;
    }

    public List<T> getOfflineAdded() {
        return this.offlineAdded;
    }

    public List<T> getOfflineRemoved() {
        return this.offlineRemoved;
    }

    public List<T> getOfflineUpdated() {
        return offlineUpdated;
    }

    public int getOfflineAddedCount() {
        return this.offlineAdded.size();
    }

    public int getOfflineRemovedCount() {
        return this.offlineRemoved.size();
    }
}
