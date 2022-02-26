package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.core.states.UserInputState
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.common.Constants
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class UpdateSaveCollectionUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(collectionId: Int, oldName: String, newName: String, collections: List<SaveCollectionModel>): UserInputState {
        val sanitized = newName.trim()
        return when {
            oldName == newName -> {
                UserInputState.NoInput
            }
            newName.length > Constants.MAX_COLLECTION_NAME_LENGTH -> {
                UserInputState.LongInput
            }
            sanitized.isEmpty() -> {
                UserInputState.EmptyInput
            }
            collections.any { it.collectionName == sanitized } -> {
                UserInputState.SameInput(Unit)
            }
            else -> {
                val userId = userRepository.getUser()

                saveRepository.updateSaveCollection(SaveCollection(
                    userId = userId,
                    collectionName = sanitized,
                    id = collectionId
                ))

                UserInputState.Valid(sanitized)
            }
        }
    }
}