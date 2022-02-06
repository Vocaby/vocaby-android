package com.vocaby.application.feature_dictionary_custom.domain.repository

import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import java.util.*

interface CustomDictionaryRepository {
    suspend fun replaceUser(newUserId: Int)
    suspend fun filterUserEntries(userId: Int, prefix: String): LinkedList<UserEntry>
    suspend fun getUserEntries(userId: Int): LinkedList<UserEntry>
    suspend fun getUserEntryId(userId: Int, entry: String): Int?
    suspend fun getUserEntryData(userId: Int, entry: String): EntryModel?
    suspend fun removeUserEntry(entryId: Int)
    suspend fun removeUserEntry(entry: String)
    suspend fun clearUserEntries(userId: Int)
    suspend fun getAllUserEntries(userId: Int): List<EntryModel>
    suspend fun insertOrUpdateEntry(
        userId: Int,
        entry: String,
        pronunciation: String,
        groupChanges: ItemChangeState<DefinitionGroupModel>,
        definitionChangesMap: MutableMap<String, ItemChangeState<DefinitionModel>>,
        saveTime: Date
    ): Int
    suspend fun insertNewEntries(userId: Int, data: List<EntryModel>)
    suspend fun getTypes(): List<String>
}