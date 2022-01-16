package com.vocaby.application.feature_customdictionary.domain.repository

import com.vocaby.application.feature_customdictionary.domain.model.DefinitionChanges
import com.vocaby.application.feature_customdictionary.domain.model.GroupChanges
import com.vocaby.application.feature_customdictionary.domain.model.UserEntry
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import java.util.*

interface CustomDictionaryRepository {
    suspend fun getUserEntries(userId: Int): LinkedList<UserEntry>
    suspend fun getUserEntryData(userId: Int, entry: String): EntryModel?
    suspend fun removeCustomEntry(entryId: Int)
    suspend fun removeCustomEntry(entry: String)
    suspend fun clearUserEntries(userId: Int)
    suspend fun getAllUserEntries(userId: Int): List<EntryModel>
    suspend fun insertOrUpdateEntry(
        userId: Int,
        entry: String,
        pronunciation: String,
        groupChanges: GroupChanges,
        definitionChangesMap: MutableMap<String, DefinitionChanges>,
        saveTime: Date
    ): Int
    suspend fun insertNewEntries(userId: Int, data: List<EntryModel>)
    suspend fun getTypes(): List<String>
}