package com.vocaby.application.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.vocaby.application.models.dictionary.EntryModel
import java.lang.reflect.Type


class EntryDeserializer: JsonDeserializer<EntryModel> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EntryModel {
        val jsonObject = json.asJsonObject
        val word: String = jsonObject.get("word").asString
        val entryData = EntryModel(word)
        entryData.lastUpdated = jsonObject.get("last_updated").asString
        entryData.pronunciation = jsonObject.get("pronunciation").asString
        val data: JsonObject = jsonObject.get("definitions").asJsonObject

        for (type in data.keySet()) {
            val definitions = data[type].asJsonArray
            for (jsonElement in definitions) {
                val itemJsonObject = jsonElement.asJsonObject
                val definition = itemJsonObject["definition"].asString
                val sentence = itemJsonObject["sentence"].asString
                entryData.addDefinition(type, definition, sentence)
            }
        }

        return entryData
    }
}