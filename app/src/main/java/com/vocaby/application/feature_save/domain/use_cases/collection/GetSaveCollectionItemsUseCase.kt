package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSaveCollectionItemsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(collectionName: String): Flow<List<String>> {
        val userId = userRepository.getUser()
        return saveRepository.getCollectionItems(userId, collectionName)
    }
}