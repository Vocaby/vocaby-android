package com.vocaby.application.feature_save.domain.use_cases.collection
import com.vocaby.application.core.states.UserInputState
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.common.Constants
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class AddSaveCollectionUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(collectionName: String, collections: List<SaveCollectionModel>): UserInputState {
        val sanitized = collectionName.trim()
        return when {
            sanitized.isEmpty() -> {
                UserInputState.EmptyInput
            }
            collectionName.length > Constants.MAX_COLLECTION_NAME_LENGTH -> {
                UserInputState.LongInput
            }
            collections.any { it.collectionName == sanitized } -> {
                UserInputState.SameInput(Unit)
            }
            else -> {
                val userId = userRepository.getUser()

                val collection = SaveCollection(
                    userId,
                    sanitized
                )

                saveRepository.addSaveCollection(collection)
                UserInputState.Valid(sanitized)
            }
        }
    }
}