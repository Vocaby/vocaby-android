package com.vocaby.app.utils;

import com.vocaby.app.data.entity.Definition;
import com.vocaby.app.data.entity.WordDefinitions;
import com.vocaby.app.models.EntryModel;

public class WordService {
    public static EntryModel convertToWordModel(WordDefinitions wordDefinitions) {
        EntryModel wordData = new EntryModel(wordDefinitions.word.getWord());
        if(wordDefinitions.word.getPronunciation() != null) {
            wordData.setPronunciation(wordDefinitions.word.getPronunciation());
        } else {
            wordData.setPronunciation("");
        }

        for(Definition data : wordDefinitions.definitions) {
            wordData.addDefinition(data.getPos(), data.getDefinition(), data.getSentence());
        }

        return wordData;
    }
}
