package com.vocaby.app.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WordModel {
    private final String word;
    private String pronunciation;
    private final Map<String, List<String>> mDefinitions;
    private final Map<String, List<String>> mSentences;
    private final List<String> allowedPos;

    public WordModel(String word) {
        this.word = word;
        allowedPos = new ArrayList<>();
        mDefinitions = new HashMap<>();
        mSentences = new HashMap<>();
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getPronunciation() { return this.pronunciation; }

    public String getWord() { return word; }

    public boolean isEmpty() {
        return mDefinitions.isEmpty();
    }

    public void addDefinition(String pos, String definition) {
        if(mDefinitions.containsKey(pos)) {
            List<String> definitions = mDefinitions.get(pos);
            assert definitions != null;
            definitions.add(definition);
        } else {
            allowedPos.add(pos);
            List<String> definitions = new LinkedList<>();
            definitions.add(definition);
            mDefinitions.put(pos, definitions);
        }
    }

    public void addSentence(String pos, String sentence) {
        if(mSentences.containsKey(pos)) {
            List<String> definitions = mSentences.get(pos);
            assert definitions != null;
            definitions.add(sentence);
        } else {
            List<String> definitions = new LinkedList<>();
            definitions.add(sentence);
            mSentences.put(pos, definitions);
        }
    }

    public String[] getAllowedPos() {
        return allowedPos.toArray(new String[0]);
    }

    public String[] getDefinitions(String pos) {
        return mDefinitions.get(pos).toArray(new String[0]);
    }

    public String[] getSentences(String pos) {
        return mSentences.get(pos).toArray(new String[0]);
    }

    @Override
    public String toString() {
        return word;
    }
}
