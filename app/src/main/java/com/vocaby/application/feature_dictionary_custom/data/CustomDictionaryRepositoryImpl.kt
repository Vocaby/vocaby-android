package com.vocaby.application.feature_dictionary_custom.data

import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary_custom.data.local.CustomDictionaryDao
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomDefinition
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntry
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntryGroup
import com.vocaby.application.feature_dictionary_custom.data.local.entity.EntryWithData
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CustomDictionaryRepositoryImpl constructor(
    private val dao: CustomDictionaryDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
): CustomDictionaryRepository {
    override suspend fun replaceUser(newUserId: Int) = dao.replaceUser(newUserId)
    override suspend fun filterUserEntries(userId: Int, prefix: String): LinkedList<UserEntry> {
        val list = dao.filterUserEntries(userId, prefix)
        return LinkedList(list)
    }

    override suspend fun getUserEntries(userId: Int): LinkedList<UserEntry> = withContext(defaultDispatcher) {
        val list = dao.getUserEntries(userId)
        LinkedList(list)
    }

    override suspend fun getUserEntryId(userId: Int, entry: String): Int? = dao.getUserEntryId(userId, entry)

    override suspend fun getUserEntryData(userId: Int, entry: String): EntryModel? =
        convertCustomToEntryModel(dao.getUserEntryData(userId, entry))

    override suspend fun removeUserEntry(entryId: Int) = dao.deleteUserEntry(entryId)

    override suspend fun removeUserEntry(entry: String) = dao.deleteUserEntry(entry)

    override suspend fun clearUserEntries(userId: Int) = dao.clearUserEntries(userId)

    override suspend fun getAllUserEntries(userId: Int): List<EntryModel> {
        val entries = dao.getAllUserEntryData(userId)
        val entryModels = mutableListOf<EntryModel>()
        entries.forEach { entryWithData ->
            val model = convertCustomToEntryModel(entryWithData)
            model?.let {
                entryModels.add(model)
            }
        }

        return entryModels
    }

    override suspend fun insertOrUpdateEntry(
        userId: Int,
        entry: String,
        pronunciation: String,
        groupChanges: ItemChangeState<DefinitionGroupModel>,
        definitionChangesMap: MutableMap<String, ItemChangeState<DefinitionModel>>,
        saveTime: Date
    ): Int {
        val entryId: Int = if (groupChanges.id == -1) {
            dao.insertCustomEntry(
                CustomEntry(
                    userId,
                    entry,
                    pronunciation,
                    saveTime
                )
            ).toInt()
        } else {
            dao.updateCustomEntry(
                CustomEntry(
                    userId,
                    entry,
                    pronunciation,
                    saveTime,
                    groupChanges.id
                )
            )

            groupChanges.id
        }

        val deletedGroups = mutableListOf<CustomEntryGroup>()
        for (group in groupChanges.deletedItems) {
            deletedGroups.add(CustomEntryGroup(group.groupId, group.type))
        }

        val updatedGroups = mutableListOf<CustomEntryGroup>()
        for (group in groupChanges.updatedItems) {
            updatedGroups.add(
                CustomEntryGroup(
                    group.groupId,
                    entryId,
                    group.type,
                    group.order
                )
            )
        }

        val addedGroups = mutableListOf<CustomEntryGroup>()
        for (group in groupChanges.addedItems) {
            addedGroups.add(
                CustomEntryGroup(
                    entryId,
                    group.type,
                    group.order
                )
            )
        }

        val deletedDefinitions = mutableListOf<CustomDefinition>()
        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.deletedItems) {
                deletedDefinitions.add(
                    CustomDefinition(
                        definitionModel.id,
                        definitionChanges.id,
                        definitionModel.definition,
                        definitionModel.example,
                        definitionModel.order
                    )
                )
            }
        }

        val updatedDefinitions = mutableListOf<CustomDefinition>()
        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.updatedItems) {
                updatedDefinitions.add(
                    CustomDefinition(
                        definitionModel.id,
                        definitionChanges.id,
                        definitionModel.definition,
                        definitionModel.example,
                        definitionModel.order
                    )
                )
            }
        }

        dao.deleteCustomEntryGroups(deletedGroups)
        dao.updateCustomEntryGroups(updatedGroups)

        val ids = dao.insertCustomEntryGroups(addedGroups)
        val newGroups = groupChanges.addedItems
        for (i in newGroups.indices) {
            val definitionChanges =
                definitionChangesMap[newGroups[i].type]
            if (definitionChanges != null) definitionChanges.id =
                ids[i].toInt()
        }

        val addedDefinitions = mutableListOf<CustomDefinition>()
        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.addedItems) {
                addedDefinitions.add(
                    CustomDefinition(
                        definitionChanges.id,
                        definitionModel.definition,
                        definitionModel.example,
                        definitionModel.order
                    )
                )
            }
        }

        dao.insertCustomDefinitions(addedDefinitions)
        dao.updateCustomDefinitions(updatedDefinitions)
        dao.deleteCustomDefinitions(deletedDefinitions)

        return entryId
    }

    override suspend fun insertNewEntries(userId: Int, data: List<EntryModel>) {
        val customEntries = mutableListOf<CustomEntry>()
        for (entryData in data) {
            customEntries.add(
                CustomEntry(
                    userId,
                    entryData.entry,
                    entryData.pronunciation,
                    entryData.lastUpdated
                )
            )
        }

        clearUserEntries(userId)
        val entryIds = dao.insertCustomEntries(customEntries)
        val addedGroups = mutableListOf<CustomEntryGroup>()
        for ((i, entryId) in entryIds.withIndex()) {
            for (group in data[i].definitionGroups) {
                addedGroups.add(
                    CustomEntryGroup(
                        entryId.toInt(),
                        group.type,
                        group.order
                    )
                )
            }
        }

        val groupIds = dao.insertCustomEntryGroups(addedGroups)
        val addedDefinitions = mutableListOf<CustomDefinition>()
        var j = 0
        for (entryModel in data) {
            for (group in entryModel.definitionGroups) {
                val definitionModels = group.definitionData
                for (definitionModel in definitionModels) {
                    addedDefinitions.add(
                        CustomDefinition(
                            groupIds[j].toInt(),
                            definitionModel.definition,
                            definitionModel.example,
                            definitionModel.order
                        )
                    )
                }
                j++
            }
        }

        dao.insertCustomDefinitions(addedDefinitions)
    }

    private suspend fun convertCustomToEntryModel(data: EntryWithData?): EntryModel? =
        withContext(Dispatchers.Default) {
        data?.let {
            val entryData = EntryModel(
                data.customEntry.entryId,
                data.customEntry.entry,
                data.customEntry.pronunciation,
                data.customEntry.lastUpdated
            )

            val groups: MutableList<DefinitionGroupModel> = ArrayList()
            for (group in data.groups) {
                val groupModel = DefinitionGroupModel(
                    group.entryGroup.groupId,
                    group.entryGroup.type,
                    group.entryGroup.order
                )

                val definitions: MutableList<DefinitionModel> = ArrayList()
                for (definitionData in group.definitions) {
                    val definitionModel = DefinitionModel(
                        definitionData.definitionId,
                        group.entryGroup.type,
                        definitionData.definition,
                        definitionData.example,
                        definitionData.order
                    )
                    definitions.add(definitionModel)
                }

                definitions.sort()
                groupModel.definitionData = definitions
                groups.add(groupModel)
            }

            groups.sort()
            entryData.definitionGroups = groups
            entryData
        }
    }

    override suspend fun getTypes(): List<String> = dao.getTypes()
}