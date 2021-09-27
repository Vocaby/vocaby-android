package com.vocaby.app.models;

public class WordDataPackage {
    private final EntryModel entryData;
    private boolean saved;

    public WordDataPackage(EntryModel entryData, boolean saved) {
        this.entryData = entryData;
        this.saved = saved;
    }

    public WordDataPackage(EntryModel entryData, int result) {
        this.entryData = entryData;
        this.saved = result == 1;
    }

    public EntryModel getWordModel() {
        return this.entryData;
    }

    public Boolean saved() {
        return this.saved;
    }

    public WordDataPackage setSave(Boolean saved) {
        this.saved = saved;
        return this;
    }
}
