package com.vocaby.app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WordService {
    private final JSONObject json;
    private final Word word;
    private boolean success;
    private int code;

    public WordService(String word, JSONObject json) {
        this.word = new Word(word);
        this.json = json;
        success = false;
        code = 0;
    }

    public void parse() {
        try {
            String status = json.getString("status");
            if(status.equals("failed")) {
                success = false;
                code = -1;
            } else {
                success = true;
                String pronunciation = json.getString("pronunciation");
                word.setPronunciation(pronunciation);
                JSONArray data = json.getJSONArray("data");
                for(int i = 0; i < data.length(); i++) {
                    JSONObject definitionData = data.getJSONObject(i);
                    String pos = definitionData.getString("pos");
                    JSONArray sd = definitionData.getJSONArray("definitions");
                    for(int j = 0; j < sd.length(); j++) {
                        String definition = sd.getJSONObject(j).getString("definition");
                        String sentence = sd.getJSONObject(j).getString("sentence");
                        word.addDefinition(pos, definition);
                        word.addSentence(pos, sentence);
                    }
                }
            }
        } catch (JSONException e) {
            success = false;
        }
    }

    public Word getWordData() {
        return word;
    }

    public boolean wasSuccessful() {
        return success;
    }

    public int getCode() {
        return code;
    }
}
