package com.vocaby.app.models;

import com.vocaby.app.utils.WordService;
import com.vocaby.app.data.entity.WordDefinitions;

public class WordDataPackage {
    private final EntryModel entryData;
    private boolean saved;

    public WordDataPackage(EntryModel entryData, boolean saved) {
        this.entryData = entryData;
        this.saved = saved;
    }

    public WordDataPackage(WordDefinitions wordDefinitions, int result) {
        this.entryData = WordService.convertToWordModel(wordDefinitions);
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
