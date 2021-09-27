package com.vocaby.app.models;

public class WordDataPackage {
    private final EntryModel entryData;
    private final EntryModel customEntryData;
    private boolean saved;

    public WordDataPackage(EntryModel entryData, boolean saved) {
        this.entryData = entryData;
        customEntryData = new EntryModel(entryData.getEntry());
        this.saved = saved;
    }

    public WordDataPackage(EntryModel entryData, EntryModel customEntryData, int result) {
        this.entryData = entryData;
        this.customEntryData = customEntryData;
        this.saved = result == 1;
    }

    public EntryModel getWordModel() {
        return this.entryData;
    }

    public EntryModel getCustomEntryData() { return customEntryData; }

    public Boolean saved() {
        return this.saved;
    }

    public WordDataPackage setSave(Boolean saved) {
        this.saved = saved;
        return this;
    }
}
