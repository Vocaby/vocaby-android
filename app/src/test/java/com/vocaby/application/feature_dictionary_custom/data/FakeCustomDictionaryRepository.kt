package com.vocaby.application.feature_dictionary_custom.data

import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.feature_dictionary.domain.model.DefinitionModel
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary_custom.domain.model.ItemChangeState
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import kotlinx.coroutines.flow.Flow
import java.util.*

class FakeCustomDictionaryRepository: CustomDictionaryRepository {
    override suspend fun replaceUser(newUserId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun filterUserEntries(userId: Int, prefix: String): LinkedList<UserEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserEntries(userId: Int): LinkedList<UserEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserEntryId(userId: Int, entry: String): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getUserEntryData(userId: Int, entry: String): EntryModel? {
        TODO("Not yet implemented")
    }

    override suspend fun removeUserEntry(entryId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun removeUserEntry(entry: String) {
        TODO("Not yet implemented")
    }

    override suspend fun clearUserEntries(userId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllUserEntries(userId: Int): List<EntryModel> {
        TODO("Not yet implemented")
    }

    override suspend fun insertOrUpdateEntry(
        userId: Int,
        entry: String,
        pronunciation: String,
        groupChanges: ItemChangeState<DefinitionGroupModel>,
        definitionChangesMap: MutableMap<String, ItemChangeState<DefinitionModel>>,
        saveTime: Date
    ): Int {
        TODO("Not yet implemented")
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