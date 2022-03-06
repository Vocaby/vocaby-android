package com.vocaby.application.feature_dictionary.data.remote

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import java.lang.reflect.Type


class EntryDeserializer: JsonDeserializer<EntryModel> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): EntryModel {
        val jsonObject = json.asJsonObject
        val entry: String = jsonObject.get("entry").asString
        val entryData = EntryModel(entry = entry)
        entryData.lastUpdated = Formatter.formatStringToDate(jsonObject.get("last_updated").asString)
        entryData.pronunciation = jsonObject.get("pronunciation").asString
        entryData.description = jsonObject.get("description").asString
        val data: JsonObject = jsonObject.get("definitions").asJsonObject

        for (type in data.keySet()) {
            val definitions = data[type].asJsonArray
            for (jsonElement in definitions) {
                val itemJsonObject = jsonElement.asJsonObject
                val definition = itemJsonObject["definition"].asString
                val example = itemJsonObject["example"].asString
                entryData.addDefinition(type, definition, example)
            }
        }

        return entryData
    }
}