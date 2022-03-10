package com.vocaby.application.feature_datatransfer.data

import android.content.ContentResolver
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.Parser
import com.vocaby.application.core.util.Validator
import com.vocaby.application.core.util.exceptions.IllegalFileException
import com.vocaby.application.feature_datatransfer.domain.model.EntryTransferModel
import com.vocaby.application.feature_datatransfer.domain.model.ExportModel
import com.vocaby.application.feature_datatransfer.domain.model.SaveTransferModel
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_profile.common.Constants
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.CollectionItemModel
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
    override suspend fun readSavesFromExternalStorage(uri: Uri, userId: Int): SaveTransferModel = withContext(defaultDispatcher) {
        val extension = Parser.parseFileExtensionFromUri(uri, contentResolver)
        if (!extension.startsWith("json")) {
            // Process files without an extension as well
            if (extension.isNotEmpty()) {
                throw IllegalFileException(
                    "The file has the wrong extension - $extension",
                    IllegalFileException.INVALID_FILE
                )
            }
        }

        val saves: MutableList<UserSave> = mutableListOf()
        val collectionMap: MutableMap<String, List<CollectionItemModel>> = mutableMapOf()
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
                        for (i in allSaves) {
                            yield()
                            val item = i.asJsonObject
                            val entry = Formatter.cleanText(item.getAsJsonPrimitive("entry").asString)
                            val lastUpdated = item.getAsJsonPrimitive("lastSaved").asString

                            if (!Validator.dateIsValid(lastUpdated)) {
                                throw IllegalFileException(
                                    "The file is malformed",
                                    IllegalFileException.INVALID_FILE
                                )
                            }

                            val entryIsValid = Validator.entryIsValid(entry)
                            if (entryIsValid) {
                                saves.add(UserSave(
                                    userId,
                                    entry,
                                    lastSaved = lastUpdated
                                ))
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
                            val savesInCollection = mutableListOf<CollectionItemModel>()
                            for (i in collections.getAsJsonArray(collection)) {
                                yield()
                                val collectionItem = i.asJsonObject
                                val entry = Formatter.cleanText(collectionItem.getAsJsonPrimitive("entry").asString)
                                val lastAdded = collectionItem.getAsJsonPrimitive("lastAdded").asString

                                val entryIsValid = Validator.entryIsValid(entry)

                                if (!Validator.dateIsValid(lastAdded)) {
                                    throw IllegalFileException(
                                        "The file is malformed",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                if (entryIsValid) {
                                    savesInCollection.add(CollectionItemModel(entry, lastAdded = lastAdded))
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

    override suspend fun readCustomEntriesFromExternalStorage(uri: Uri): List<EntryModel> = withContext(defaultDispatcher)  {
        val extension = Parser.parseFileExtensionFromUri(uri, contentResolver)
        if (!extension.startsWith("json")) {
            // Process files without an extension as well
            if (extension.isNotEmpty()) {
                throw IllegalFileException(
                    "The file has the wrong extension - $extension",
                    IllegalFileException.INVALID_FILE
                )
            }
        }

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
                                val entry = Formatter.cleanText(item.getAsJsonPrimitive("entry").asString)
                                val pronunciation = Formatter.cleanText(item.getAsJsonPrimitive("pronunciation").asString, false)
                                val description = if (item.getAsJsonPrimitive("description") == null) null
                                    else Formatter.cleanText(item.getAsJsonPrimitive("description").asString, false)
                                val lastUpdated = item.getAsJsonPrimitive("lastUpdated").asString

                                // Validations
                                val entryIsValid = Validator.entryIsValid(entry)
                                if (!entryIsValid) {
                                    throw IllegalFileException(
                                        "$entry is not valid",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                if (!Validator.dateIsValid(lastUpdated)) {
                                    throw IllegalFileException(
                                        "The file is malformed",
                                        IllegalFileException.INVALID_FILE
                                    )
                                }

                                val exists = entryModels.any { it.entry == entry }
                                val parsedGroups = mutableListOf<String>()
                                if (!exists) {
                                    val entryModel = EntryModel(entry = entry, pronunciation = pronunciation, description = description, lastUpdated = Formatter.formatStringToDate(lastUpdated))
                                    for (g in item.getAsJsonArray("definitionGroups")) {
                                        yield()
                                        val group = g.asJsonObject
                                        val type = Formatter.cleanText(group.getAsJsonPrimitive("type").asString)
                                        val order = group.getAsJsonPrimitive("order").asInt
                                        val groupModel = DefinitionGroupModel(type, order)

                                        val parsedDefinitions = mutableListOf<String>()
                                        if (type.isNotEmpty() && !parsedGroups.contains(type)) {
                                            for (d in group.getAsJsonArray("definitionData")) {
                                                yield()
                                                val definitionData = d.asJsonObject
                                                val definition =
                                                    Formatter.cleanText(definitionData.getAsJsonPrimitive("definition").asString, false)

                                                val exampleJsonPrimitive =
                                                    definitionData.getAsJsonPrimitive("example")
                                                val example: String? =
                                                    if (exampleJsonPrimitive.isString) {
                                                        Formatter.cleanText(exampleJsonPrimitive.asString, false)
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