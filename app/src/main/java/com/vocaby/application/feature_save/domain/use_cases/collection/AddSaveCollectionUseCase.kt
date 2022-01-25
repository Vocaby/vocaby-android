package com.vocaby.application.feature_save.domain.use_cases.collection
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.common.Constants
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import com.vocaby.application.states.UserInputState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AddSaveCollectionUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(collectionName: String, collections: List<SaveCollectionModel>): Flow<UserInputState> = flow {
        val sanitized = collectionName.trim()
        when {
            sanitized.isEmpty() -> {
                emit(UserInputState.EmptyInput)
            }

            collectionName.length > Constants.MAX_COLLECTION_NAME_LENGTH -> {
                emit(UserInputState.LongInput)
            }

            else -> {
                val userId = userRepository.getUser()
                if (collections.any { it.name == sanitized }) {
                    emit(UserInputState.SameInput)
                } else {
                    val collection = SaveCollection(
                        userId,
                        sanitized
                    )

                    saveRepository.addSaveCollection(collection)
                    emit(UserInputState.Valid(sanitized))
                }
            }
        }
    }
}