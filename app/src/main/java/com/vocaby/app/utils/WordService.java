package com.vocaby.app.utils;

import com.vocaby.app.data.entity.Definition;
import com.vocaby.app.data.entity.WordDefinitions;
import com.vocaby.app.models.WordModel;

public class WordService {
    public static WordModel convertToWordModel(WordDefinitions wordDefinitions) {
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

        return wordData;
    }
}
