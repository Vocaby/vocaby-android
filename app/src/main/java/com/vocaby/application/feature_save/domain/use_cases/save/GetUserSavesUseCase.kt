package com.vocaby.application.feature_save.domain.use_cases.save

import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSavesUseCase @Inject constructor(
    private val saveRepository: SaveRepository
) {
    operator fun invoke(currentUser: Int): Flow<List<String>> {
        return saveRepository.getAllSavedEntriesFlow(currentUser)
    }

}