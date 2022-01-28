package com.vocaby.application.feature_save.domain.use_cases.collection

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import com.vocaby.application.states.UserInputState
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
            sanitized.isEmpty() -> {
                UserInputState.EmptyInput
            }
            collections.any { it.collectionName == sanitized } -> {
                UserInputState.SameInput
            }
            else -> {
                val userId = userRepository.getUser()

                saveRepository.updateSaveCollection(SaveCollection(
                    userId = userId,
                    collectionName = newName,
                    id = collectionId
                ))

                UserInputState.Valid(sanitized)
            }
        }
    }
}