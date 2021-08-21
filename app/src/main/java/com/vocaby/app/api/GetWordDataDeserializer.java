package com.vocaby.app.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vocaby.app.models.Word;


import java.lang.reflect.Type;

public class GetWordDataDeserializer implements JsonDeserializer<Word> {
    @Override
    public Word deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        String status = jsonObject.get("status").getAsString();
        String word = jsonObject.get("word").getAsString();
        Word wordData =  new Word(word);
        if(status.equals("success")) {

            String pronunciation = jsonObject.get("pronunciation").getAsString();


            wordData.setPronunciation(pronunciation);

            JsonObject data = jsonObject.get("definitions").getAsJsonObject();
            for (String key : data.keySet()) {
                JsonArray definitions = data.get(key).getAsJsonArray();
                for(JsonElement jsonElement : definitions) {
                    final JsonObject itemJsonObject = jsonElement.getAsJsonObject();
                    String definition = itemJsonObject.get("definition").getAsString();
                    String sentence = itemJsonObject.get("sentence").getAsString();
                    wordData.addDefinition(key, definition);
                    wordData.addSentence(key, sentence);
                }
            }
        }

        return wordData;
    }
}
