package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.*
import java.util.*

class GetSaveCollectionsUseCase(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(): Flow<List<SaveCollectionModel>> {
        val userId = userRepository.getUser()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000)
            set(Calendar.MONTH, 3)
        }

        val savesCount = saveRepository.getAllSavesCount(userId)
        val userSavesAsCollection = SaveCollectionModel("All Saves", calendar.time, savesCount)
        val collections = saveRepository.getSaveCollections(userId).map { collections ->
            val list = collections.toMutableList()
            list.add(0, userSavesAsCollection)
            list
        }

        return collections
    }
}