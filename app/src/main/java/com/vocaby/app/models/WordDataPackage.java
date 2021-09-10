package com.vocaby.app.models;

import com.vocaby.app.utils.WordService;
import com.vocaby.app.data.entity.WordDefinitions;

public class WordDataPackage {
    private final WordModel wordModel;
    private boolean saved;

    public WordDataPackage(WordModel wordModel, boolean saved) {
        this.wordModel = wordModel;
        this.saved = saved;
    }

    public WordDataPackage(WordDefinitions wordDefinitions, int result) {
        this.wordModel = WordService.convertToWordModel(wordDefinitions);
        this.saved = result == 1;
    }

    public WordModel getWordModel() {
        return this.wordModel;
    }

    public Boolean saved() {
        return this.saved;
    }

    public WordDataPackage setSave(Boolean saved) {
        this.saved = saved;
        return this;
    }
}
