package com.vocaby.application.feature_dictionary_custom.data

import com.google.common.truth.Truth
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomDefinition
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntry
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntryGroup
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.repository.FakeCustomDictionaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class FakeCustomDictionaryRepositoryImpl: FakeCustomDictionaryRepository {
    private val customEntry = mutableListOf<CustomEntry>()
    private val customEntryGroup = mutableListOf<CustomEntryGroup>()
    private val customEntryDefinition = mutableListOf<CustomDefinition>()

    private fun convertToEntryModel(userId: Int, entry: String): EntryModel? {
        var entryModel: EntryModel? = null
        val cEntry = customEntry.find { it.entry == entry && it.userId == userId }
        cEntry?.let {
            entryModel = EntryModel(it.entryId, it.entry, it.pronunciation, it.lastUpdated)
            val cGroups = customEntryGroup.filter { group -> group.entryId == cEntry.entryId }
            cGroups.forEach { g ->
                val cDefinitions = customEntryDefinition.filter {
                        definition -> definition.groupId == g.groupId
                }

                val customDefinitions = cDefinitions.map { definition ->
                    DefinitionModel(g.type, definition.definition, definition.example, definition.order, definition.definitionId)
                }

                entryModel!!.addDefinitionGroup(
                    DefinitionGroupModel(g.type, g.order, g.groupId, customDefinitions.toMutableList())
                )
            }
        }

        return entryModel
    }

    override suspend fun insertEntry(
        userId: Int,
        entry: String,
        type: String,
        definition: String,
        example: String,
        definition2: String?,
        example2: String?
    ) {
        val group = DefinitionGroupModel(type, 0, -1)
        val groupChanges = ItemChangeState<DefinitionGroupModel>()
        val definitionChanges = ItemChangeState<DefinitionModel>()

        val definitionModel = DefinitionModel(
            type,
            definition,
            example,
            0
        )

        group.addNewDefinition(definitionModel)
        definitionChanges.addNew(definitionModel.definition, definitionModel)

        var definitionModel2: DefinitionModel? = null

        definition2?.let {
            definitionModel2 = DefinitionModel(
                type,
                it,
                example2,
                1
            )
        }

        definitionModel2?.let {
            group.addNewDefinition(it)
            definitionChanges.addNew(it.definition, it)
        }

        groupChanges.addNew(type, group)

        val mutableMap = mutableMapOf(
            type to definitionChanges
        )

        insertOrUpdateEntry(userId, entry, "", groupChanges, mutableMap, Date())
    }

    override suspend fun updateEntry(
        userId: Int,
        entryModel: EntryModel,
        newDefinition: String,
        newExample: String
    ) {
        val group = entryModel.definitionGroups.first()
        val definitionData = group.definitionData.first()
        val groupChanges = ItemChangeState<DefinitionGroupModel>()
        val definitionChanges = ItemChangeState<DefinitionModel>()

        definitionChanges.id = group.groupId
        definitionChanges.updateExisting(definitionData.id, DefinitionModel(group.type, newDefinition, newExample, definitionData.order, definitionData.id))

        val mutableMap = mutableMapOf(
            group.type to definitionChanges
        )

        insertOrUpdateEntry(userId, entryModel.entry, "", groupChanges, mutableMap, Date())
    }

    override suspend fun replaceUser(newUserId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun filterUserEntries(userId: Int, prefix: String): LinkedList<UserEntry?> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserEntries(
        userId: Int,
        limit: Int,
        offset: Int
    ): LinkedList<UserEntry?> {
        TODO("Not yet implemented")
    }

    override fun getUserEntriesCount(userId: Int): Flow<Int> = flow {
        emit(customEntry.size)
    }

    override suspend fun getUserEntryId(userId: Int, entry: String): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getUserEntryData(userId: Int, entry: String): EntryModel? {
        return convertToEntryModel(userId, entry)
    }

    override suspend fun removeUserEntry(entryId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun removeUserEntry(entry: String) {
        val index = customEntry.indexOfFirst { it.entry == entry }
        if (index != -1) {
            val custom = customEntry.removeAt(index)

            val deletedGroups = mutableListOf<CustomEntryGroup>()
            val iterator = customEntryGroup.iterator()
            while(iterator.hasNext()){
                val group = iterator.next()
                if (group.entryId == custom.entryId) {
                    deletedGroups.add(group)
                    iterator.remove()
                }
            }

            deletedGroups.forEach { group ->
                customEntryDefinition.removeAll { it.groupId == group.groupId }
            }
        }
    }

    override suspend fun clearUserEntries(userId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllUserEntries(userId: Int): List<EntryModel> {
        return customEntry.filter { it.userId == userId }.map { convertToEntryModel(userId, it.entry)!! }
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
            val newEntry = CustomEntry(
                userId,
                entry,
                pronunciation,
                saveTime,
                customEntry.size+1
            )

            customEntry.add(newEntry)
            newEntry.entryId
        } else {
            val index = customEntry.indexOfFirst { it.entryId == groupChanges.id }
            customEntry[index] = CustomEntry(
                userId,
                entry,
                pronunciation,
                saveTime,
                groupChanges.id
            )

            groupChanges.id
        }

        for (group in groupChanges.deletedItems) {
            customEntryGroup.removeIf { it.groupId == group.groupId }
        }

        for (group in groupChanges.updatedItems) {
            val index = customEntryGroup.indexOfFirst { it.groupId == group.groupId }
            if (index != -1) {
                customEntryGroup[index] =
                    CustomEntryGroup(
                        group.groupId,
                        entryId,
                        group.type,
                        group.order
                    )
            }
        }

        val addedGroupIds = mutableListOf<Int>()
        for (group in groupChanges.addedItems) {
            addedGroupIds.add(customEntryGroup.size + 1)
            customEntryGroup.add(
                CustomEntryGroup(
                    groupId = customEntryGroup.size + 1,
                    entryId,
                    group.type,
                    group.order
                )
            )
        }

        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.deletedItems) {
                customEntryDefinition.removeIf { it.definitionId == definitionModel.id }
            }
        }

        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.updatedItems) {
                val index = customEntryDefinition.indexOfFirst { it.definitionId == definitionModel.id }
                if (index != -1) {
                    customEntryDefinition[index] =
                        CustomDefinition(
                            definitionModel.id,
                            definitionChanges.id,
                            definitionModel.definition,
                            definitionModel.example,
                            definitionModel.order
                        )
                }
            }
        }

        val newGroups = groupChanges.addedItems
        for (i in newGroups.indices) {
            val definitionChanges =
                definitionChangesMap[newGroups[i].type]
            if (definitionChanges != null) definitionChanges.id = addedGroupIds[i]
        }

        for (definitionChanges in definitionChangesMap.values) {
            for (definitionModel in definitionChanges.addedItems) {
                customEntryDefinition.add(
                    CustomDefinition(
                        definitionId = customEntryDefinition.size + 1,
                        groupId = definitionChanges.id,
                        definitionModel.definition,
                        definitionModel.example,
                        definitionModel.order
                    )
                )
            }
        }

        return entryId
    }

    override suspend fun insertNewEntries(userId: Int, data: List<EntryModel>) {
        TODO("Not yet implemented")
    }

    override suspend fun insertTypes(types: List<Type>) {
        TODO("Not yet implemented")
    }

    override suspend fun removeTypes(types: List<Type>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateTypes(types: List<Type>) {
        TODO("Not yet implemented")
    }

    override suspend fun clearTypes() {
        TODO("Not yet implemented")
    }

    override fun getTypes(): Flow<List<Type>> {
        TODO("Not yet implemented")
    }
}