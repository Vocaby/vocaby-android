package com.vocaby.app.models.datapackage;

import com.vocaby.app.models.dictionary.EntryModel;

public class EntryDataPackage {
    private final EntryModel originalEntryData;
    private final EntryModel customEntryData;
    private final boolean saved;

    public EntryDataPackage(EntryModel originalEntryData, EntryModel customEntryData, boolean saved) {
        this.originalEntryData = originalEntryData;
        this.customEntryData = customEntryData;
        this.saved = saved;
    }

    public EntryDataPackage(EntryModel originalEntryData, EntryModel customEntryData, int result) {
        this.originalEntryData = originalEntryData;
        this.customEntryData = customEntryData;
        this.saved = result == 1;
    }

    public EntryDataPackage(EntryModel originalEntryData, boolean saved) {
        this.originalEntryData = originalEntryData;
        this.customEntryData = new EntryModel(originalEntryData.getEntry());
        this.saved = saved;
    }

    public EntryModel getOriginalData() {
        return this.originalEntryData;
    }

    public EntryModel getCustomData() {
        return this.customEntryData;
    }

    public EntryModel getEntryData() {
        if (customEntryAvailable()) {
            return this.customEntryData;
        } else {
            return this.originalEntryData;
        }
    }

    public boolean bothDataAvailable() {
        return !this.originalEntryData.isEmpty() && !this.customEntryData.isEmpty();
    }

    public boolean onlyCustomAvailable() {
        return this.originalEntryData.isEmpty() && !this.customEntryData.isEmpty();
    }

    public boolean customEntryAvailable() {
        return !this.customEntryData.isEmpty();
    }

    public Boolean saved() {
        return this.saved;
    }
}
