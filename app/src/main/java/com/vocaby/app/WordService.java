package com.vocaby.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WordService {
    private final JSONObject json;
    private final Word word;
    private boolean success;

    public WordService(String word, JSONObject json) {
        this.word = new Word(word);
        this.json = json;
        success = false;
    }

    public void parse() {
        try {
            JSONArray results = json.getJSONArray("results").getJSONObject(0).getJSONArray("lexicalEntries");
            for(int i = 0; i < results.length(); i++) {
                JSONObject entry = results.getJSONObject(i); // lexicalEntries
                String pos = entry.getJSONObject("lexicalCategory").getString("id");
                JSONObject data = entry.getJSONArray("entries").getJSONObject(0);

                String pronunciation = data.getJSONArray("pronunciations").getJSONObject(0).getString("phoneticSpelling");
                word.setPronunciation(pronunciation);

                JSONArray definitionData = data.getJSONArray("senses");
                for(int j = 0; j < definitionData.length(); j++) {
                    if(definitionData.getJSONObject(j).has("definitions")) {
                        String definition = definitionData.getJSONObject(j).getJSONArray("definitions").getString(0);
                        JSONObject defObj = definitionData.getJSONObject(j);
                        String sentence;
                        if(defObj.has("examples")) {
                            sentence = defObj.getJSONArray("examples").getJSONObject(0).getString("text");
                        } else {
                            sentence = "";
                        }

                        word.addDefinition(pos, definition);
                        word.addSentence(pos, sentence);
                    }
                }
            }

            success = true;
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
}
