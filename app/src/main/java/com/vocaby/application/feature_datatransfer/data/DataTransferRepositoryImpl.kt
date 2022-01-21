package com.vocaby.application.feature_datatransfer.data

import android.content.ContentResolver
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.exceptions.IllegalFileException
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_user.common.Constants
import com.vocaby.application.feature_user.domain.model.ExportModel
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.ArrayList

class DataTransferRepositoryImpl(
    private val contentResolver: ContentResolver,
): DataTransferRepository {
    override fun importSavesFromExternalStorage(uri: Uri): List<String> {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream.use { ins ->
            JsonReader(InputStreamReader(ins)).use { jsonReader ->
                try {
                    val gson = Gson()
                    val jsonObject = gson.fromJson<JsonObject>(jsonReader, JsonObject::class.java)
                    val saves: MutableList<String> = ArrayList()
                    if (jsonObject.has(Constants.EXPORT_TYPE_FIELD)) {
                        if (jsonObject.getAsJsonPrimitive(Constants.EXPORT_TYPE_FIELD).asString
                            == Constants.EXPORT_SAVE_TYPE
                        ) {
                            for (item in jsonObject.getAsJsonArray("data")) {
                                val entry = item.asString.lowercase()
                                val entryIsValid = Formatter.validateEntry(entry)
                                if (entryIsValid) {
                                    saves.add(entry)
                                } else {
                                    throw IllegalFileException(
                                        "$entry is not valid",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }
                            }

                            return saves
                        } else {
                            throw IllegalFileException(
                                "Backup file has the wrong entry type",
                                IllegalFileException.INVALID_FILE
                            )
                        }
                    } else {
                        throw IllegalFileException(
                            "File is json but not Vocaby's backup",
                            IllegalFileException.INVALID_FORMAT
                        )
                    }
                } catch (e: JsonSyntaxException) {
                    throw IllegalFileException(
                        e.message,
                        IllegalFileException.INVALID_FORMAT
                    )
                }
            }
        }
    }

    override fun importEntriesBackupFromExternalStorage(uri: Uri, availableTypes: List<Type>): List<EntryModel> {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream.use { ins ->
            JsonReader(InputStreamReader(ins)).use { jsonReader ->
                try {
                    val gson = Gson()
                    val jsonObject = gson.fromJson<JsonObject>(jsonReader, JsonObject::class.java)
                    val entryModels: MutableList<EntryModel> = ArrayList()
                    if (jsonObject.has(Constants.EXPORT_TYPE_FIELD)) {
                        if (jsonObject.getAsJsonPrimitive(Constants.EXPORT_TYPE_FIELD).asString
                            == Constants.EXPORT_ENTRY_TYPE
                        ) {
                            for (i in jsonObject.getAsJsonArray("data")) {
                                val item = i.asJsonObject
                                val entry = item.getAsJsonPrimitive("entry").asString.lowercase()
                                val pronunciation = item.getAsJsonPrimitive("pronunciation").asString
                                val lastUpdated = item.getAsJsonPrimitive("lastUpdated").asString

                                // Validations
                                val entryIsValid = Formatter.validateEntry(entry)
                                if (!entryIsValid) {
                                    throw IllegalFileException(
                                        "$entry is not valid",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                if (!Formatter.dateIsValid(lastUpdated)) {
                                    throw IllegalFileException(
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                val exists = entryModels.any { it.entry == entry }
                                val parsedGroups = mutableListOf<String>()
                                if (!exists) {
                                    val entryModel = EntryModel(entry = entry, pronunciation = pronunciation)
                                    for (g in item.getAsJsonArray("definitionGroups")) {
                                        val group = g.asJsonObject
                                        val type = group.getAsJsonPrimitive("type").asString.lowercase()
                                        val order = group.getAsJsonPrimitive("order").asInt
                                        val groupModel = DefinitionGroupModel(type, order)

                                        val parsedDefinitions = mutableListOf<String>()
                                        if (type.isNotEmpty() && availableTypes.any { it.type == type } && !parsedGroups.contains(type)) {
                                            for (d in group.getAsJsonArray("definitionData")) {
                                                val definitionData = d.asJsonObject
                                                val definition =
                                                    definitionData.getAsJsonPrimitive("definition").asString

                                                val exampleJsonPrimitive =
                                                    definitionData.getAsJsonPrimitive("example")
                                                val example: String? =
                                                    if (exampleJsonPrimitive.isString) {
                                                        exampleJsonPrimitive.asString
                                                    } else null

                                                val definitionOrder =
                                                    definitionData.getAsJsonPrimitive("order").asInt

                                                if (definition.isNotEmpty() && !parsedDefinitions.contains(
                                                        definition
                                                    )
                                                ) {
                                                    groupModel.addNewDefinition(
                                                        DefinitionModel(
                                                            type,
                                                            definition,
                                                            example,
                                                            definitionOrder
                                                        )
                                                    )
                                                    parsedDefinitions.add(definition)
                                                }
                                            }

                                            if (!groupModel.isEmpty) {
                                                parsedGroups.add(type)
                                                entryModel.addDefinitionGroup(groupModel)
                                            }
                                        }
                                    }

                                    if (!entryModel.isEmpty) {
                                        entryModels.add(entryModel)
                                    }
                                }
                            }

                            return entryModels
                        } else {
                            throw IllegalFileException(
                                "Backup file has the wrong entry type",
                                IllegalFileException.INVALID_FILE
                            )
                        }
                    } else {
                        throw IllegalFileException(
                            "File is json but not Vocaby's backup",
                            IllegalFileException.INVALID_FORMAT
                        )
                    }
                } catch (e: JsonSyntaxException) {
                    throw IllegalFileException(
                        e.stackTraceToString(),
                        IllegalFileException.INVALID_FORMAT
                    )
                }
            }
        }
    }

    override fun writeSavesToExternalStorage(saves: List<String>, uri: Uri) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val exportData = ExportModel(Constants.EXPORT_SAVE_TYPE, saves)
            val gson = Gson()
            gson.toJson(exportData, bw)
            bw.flush()
            bw.close()
        }
    }

    override fun writeEntriesToExternalStorage(entries: List<EntryModel>, uri: Uri) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val exportData = ExportModel(Constants.EXPORT_ENTRY_TYPE, entries)
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create()

            gson.toJson(exportData, bw)
            bw.flush()
            bw.close()
        }
    }
}