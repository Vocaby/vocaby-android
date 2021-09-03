package com.vocaby.app.api;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class GetAuthDeserializer implements JsonDeserializer<AuthResponse> {
    @Override
    public AuthResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        if(!jsonObject.has("status")) {
            String token = "Token " + jsonObject.get("token").getAsString();
            String firstName = jsonObject.get("first_name").getAsString();
            String lastName = jsonObject.get("last_name").getAsString();
            JsonArray responseSaves = jsonObject.get("saves").getAsJsonArray();
            ArrayList<String> saves = new ArrayList<>();
            for(JsonElement wordElement : responseSaves) {
                saves.add(wordElement.getAsString());
            }

            return new AuthResponse(token, saves, firstName, lastName);
        }

        return null;
    }
}
