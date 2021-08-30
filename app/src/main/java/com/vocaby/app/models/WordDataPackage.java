package com.vocaby.app.models;

import com.vocaby.app.database.entity.Definition;
import com.vocaby.app.database.entity.WordDefinitions;

public class WordDataPackage {
    private final WordModel wordModel;
    private boolean saved;

    public WordDataPackage(WordModel wordModel, boolean saved) {
        this.wordModel = wordModel;
        this.saved = saved;
    }

    public WordDataPackage(WordDefinitions wordDefinitions, int result) {
        WordModel wordData = new WordModel(wordDefinitions.word.getWord());
        if(wordDefinitions.word.getPronunciation() != null) {
            wordData.setPronunciation(wordDefinitions.word.getPronunciation());
        } else {
            wordData.setPronunciation("");
        }

        for(Definition data : wordDefinitions.definitions) {
            wordData.addDefinition(data.getPos(), data.getDefinition());
            wordData.addSentence(data.getPos(), data.getSentence());
        }

        this.wordModel = wordData;
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
