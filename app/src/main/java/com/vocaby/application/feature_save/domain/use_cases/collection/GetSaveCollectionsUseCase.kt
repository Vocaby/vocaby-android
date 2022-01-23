package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import java.util.*

class GetSaveCollectionsUseCase(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(): List<SaveCollectionModel> {
        val userId = userRepository.getUser()
        val collections = saveRepository.getSaveCollections(userId).toMutableList()
        collections.sortBy { it.lastUpdated }

        val allSavesCount = saveRepository.getAllSavesCount(userId)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000)
            set(Calendar.MONTH, 3)
        }

        collections.add(0, SaveCollectionModel("All Saves", calendar.time, allSavesCount))
        return collections
    }
}