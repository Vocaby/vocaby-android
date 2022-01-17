package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.feature_user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class GetSaveUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(entry: String): Flow<Boolean> {
        val userId = userRepository.getUser()
        return userRepository.hasSaved(userId, entry).transform { emit(it != 0) }
    }
}