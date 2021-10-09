package com.vocaby.app.models;

public class EntryDataPackage {
    private final EntryModel entryData;
    private final boolean saved;

    public EntryDataPackage(EntryModel entryData, boolean saved) {
        this.entryData = entryData;
        this.saved = saved;
    }

    public EntryDataPackage(EntryModel entryData, int result) {
        this.entryData = entryData;
        this.saved = result == 1;
    }

    public EntryModel getWordModel() {
        return this.entryData;
    }

    public Boolean saved() {
        return this.saved;
    }
}
