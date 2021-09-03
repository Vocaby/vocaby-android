package com.vocaby.app.utils;

import com.vocaby.app.database.entity.Definition;
import com.vocaby.app.database.entity.WordDefinitions;
import com.vocaby.app.models.WordModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

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
