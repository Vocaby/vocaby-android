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
import com.vocaby.application.feature_datatransfer.domain.model.EntryTransferModel
import com.vocaby.application.feature_datatransfer.domain.model.SaveTransferModel
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_profile.common.Constants
import com.vocaby.application.feature_profile.domain.model.ExportModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Suppress("BlockingMethodInNonBlockingContext")
class DataTransferRepositoryImpl(
    private val contentResolver: ContentResolver,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
): DataTransferRepository {
    override suspend fun importSavesFromExternalStorage(uri: Uri): SaveTransferModel = withContext(defaultDispatcher) {
        val saves: MutableList<String> = mutableListOf()
        val collectionMap: MutableMap<String, List<String>> = mutableMapOf()
        val inputStream = contentResolver.openInputStream(uri)
        inputStream.use { ins ->
            JsonReader(InputStreamReader(ins)).use { jsonReader ->
                val gson = Gson()
                val jsonObject = gson.fromJson<JsonObject>(jsonReader, JsonObject::class.java)
                if (jsonObject.has(Constants.EXPORT_TYPE_FIELD)) {
                    if (jsonObject.getAsJsonPrimitive(Constants.EXPORT_TYPE_FIELD).asString
                        == Constants.EXPORT_SAVE_TYPE
                    ) {
                        val data = jsonObject.getAsJsonObject("data")
                        val allSaves = data.getAsJsonArray("savedEntries")
                        val collections = data.getAsJsonObject("collections")
                        for (item in allSaves) {
                            yield()
                            val entry = item.asString.lowercase().trim()
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

                        for (collection in collections.keySet()) {
                            val sanitizedCollection = collection.trim()
                            yield()
                            val savesInCollection = mutableListOf<String>()
                            for (save in collections.getAsJsonArray(collection)) {
                                yield()
                                val entry = save.asString.lowercase().trim()
                                val entryIsValid = Formatter.validateEntry(entry)
                                if (entryIsValid) {
                                    savesInCollection.add(entry)
                                } else {
                                    throw IllegalFileException(
                                        "$entry is not valid",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }
                            }

                            if (collectionMap.containsKey(sanitizedCollection)) {
                                throw IllegalFileException(
                                    "spaced collection",
                                    IllegalFileException.INVALID_FILE
                                )
                            } else collectionMap[sanitizedCollection] = savesInCollection
                        }
                    } else {
                        throw IllegalFileException(
                            "Backup file has the wrong backup type",
                            IllegalFileException.INVALID_FILE
                        )
                    }
                } else {
                    throw IllegalFileException(
                        "File is json but not Vocaby's backup",
                        IllegalFileException.INVALID_FORMAT
                    )
                }
            }
        }

        return@withContext SaveTransferModel(saves, collectionMap)
    }

    override suspend fun importEntriesBackupFromExternalStorage(uri: Uri): List<EntryModel> = withContext(defaultDispatcher)  {
        val entryModels: MutableList<EntryModel> = ArrayList()
        val inputStream = contentResolver.openInputStream(uri)
        inputStream.use { ins ->
            JsonReader(InputStreamReader(ins)).use { jsonReader ->
                try {
                    val gson = Gson()
                    val jsonObject = gson.fromJson<JsonObject>(jsonReader, JsonObject::class.java)
                    if (jsonObject.has(Constants.EXPORT_TYPE_FIELD)) {
                        if (jsonObject.getAsJsonPrimitive(Constants.EXPORT_TYPE_FIELD).asString
                            == Constants.EXPORT_ENTRY_TYPE
                        ) {
                            val data = jsonObject.getAsJsonObject("data")
                            for (i in data.getAsJsonArray("customEntries")) {
                                yield()
                                val item = i.asJsonObject
                                val entry = item.getAsJsonPrimitive("entry").asString.lowercase().trim()
                                val pronunciation = item.getAsJsonPrimitive("pronunciation").asString.trim()
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
                                        "The file is malformed",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                val exists = entryModels.any { it.entry == entry }
                                val parsedGroups = mutableListOf<String>()
                                if (!exists) {
                                    val entryModel = EntryModel(entry = entry, pronunciation = pronunciation, lastUpdated = Formatter.formatStringToDate(lastUpdated))
                                    for (g in item.getAsJsonArray("definitionGroups")) {
                                        yield()
                                        val group = g.asJsonObject
                                        val type = group.getAsJsonPrimitive("type").asString.lowercase().trim()
                                        val order = group.getAsJsonPrimitive("order").asInt
                                        val groupModel = DefinitionGroupModel(type, order)

                                        val parsedDefinitions = mutableListOf<String>()
                                        if (type.isNotEmpty() && !parsedGroups.contains(type)) {
                                            for (d in group.getAsJsonArray("definitionData")) {
                                                yield()
                                                val definitionData = d.asJsonObject
                                                val definition =
                                                    definitionData.getAsJsonPrimitive("definition").asString.trim()

                                                val exampleJsonPrimitive =
                                                    definitionData.getAsJsonPrimitive("example")
                                                val example: String? =
                                                    if (exampleJsonPrimitive.isString) {
                                                        exampleJsonPrimitive.asString.trim()
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
                        } else {
                            throw IllegalFileException(
                                "Backup file has the wrong backup type",
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
                        "Invalid file format...",
                        IllegalFileException.INVALID_FORMAT
                    )
                }
            }
        }

        return@withContext entryModels
    }

    override suspend fun writeSavesToExternalStorage(transferModel: SaveTransferModel, uri: Uri) = withContext(defaultDispatcher)  {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val exportData = ExportModel(Constants.EXPORT_SAVE_TYPE, transferModel)
            val gson = Gson()
            gson.toJson(exportData, bw)
            bw.flush()
            bw.close()
        }
    }

    override suspend fun writeEntriesToExternalStorage(exportModel: EntryTransferModel, uri: Uri) = withContext(defaultDispatcher) {
        contentResolver.openOutputStream(uri).use { outputStream ->
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            val exportData = ExportModel(Constants.EXPORT_ENTRY_TYPE, exportModel)
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create()

            gson.toJson(exportData, bw)
            bw.flush()
            bw.close()
        }
    }
}