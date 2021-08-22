package com.vocaby.app;

import com.vocaby.app.models.WordModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class WordService {
    private final JSONObject json;
    private final WordModel wordModel;
    private boolean success;
    private int code;

    public WordService(String word, JSONObject json) {
        this.wordModel = new WordModel(word);
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
                wordModel.setPronunciation(pronunciation);
                JSONObject data = json.getJSONObject("definitions");
                for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                    String key = it.next();
                    JSONArray definitions = data.getJSONArray(key);
                    for(int i = 0; i < definitions.length() ; i++) {
                        String definition = definitions.getJSONObject(i).getString("definition");
                        String sentence = definitions.getJSONObject(i).getString("sentence");
                        wordModel.addDefinition(key, definition);
                        wordModel.addSentence(key, sentence);
                    }

                }
            }
        } catch (JSONException e) {
            success = false;
        }
    }

    public WordModel getWordData() {
        return wordModel;
    }

    public boolean wasSuccessful() {
        return success;
    }

    public int getCode() {
        return code;
    }
}
