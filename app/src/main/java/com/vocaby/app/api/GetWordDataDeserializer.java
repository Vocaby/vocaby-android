package com.vocaby.app.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vocaby.app.models.WordDataPackage;
import com.vocaby.app.models.WordModel;


import java.lang.reflect.Type;

public class GetWordDataDeserializer implements JsonDeserializer<WordDataPackage> {
    @Override
    public WordDataPackage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        String status = jsonObject.get("status").getAsString();
        String word = jsonObject.get("word").getAsString();
        boolean saved = jsonObject.get("saved").getAsInt() == 1;
        WordModel wordModelData =  new WordModel(word);
        if(status.equals("success")) {
            String pronunciation = jsonObject.get("pronunciation").getAsString();


            wordModelData.setPronunciation(pronunciation);

            JsonObject data = jsonObject.get("definitions").getAsJsonObject();
            for (String key : data.keySet()) {
                JsonArray definitions = data.get(key).getAsJsonArray();
                for(JsonElement jsonElement : definitions) {
                    final JsonObject itemJsonObject = jsonElement.getAsJsonObject();
                    String definition = itemJsonObject.get("definition").getAsString();
                    String sentence = itemJsonObject.get("sentence").getAsString();
                    wordModelData.addDefinition(key, definition);
                    wordModelData.addSentence(key, sentence);
                }
            }
        }

        return new WordDataPackage(wordModelData, saved);
    }
}
