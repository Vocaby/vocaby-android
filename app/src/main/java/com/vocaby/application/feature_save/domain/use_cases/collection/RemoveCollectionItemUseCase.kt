package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class RemoveCollectionItemUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(entry: String, collectionId: Int) {
        val userId = userRepository.getUser()
        val saveId = saveRepository.getSaveId(userId, entry)
        saveId?.let {
            saveRepository.removeCollectionItem(saveId, collectionId)
        }
    }
}