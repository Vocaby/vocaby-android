package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetAllCollectionsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(): List<SaveCollectionModel> {
        val userId = userRepository.getUser()
        val collections = saveRepository.getSaveCollections(userId).first().toMutableList()
        collections.add(0, SaveCollectionModel())
        return collections
    }
}