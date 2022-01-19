package com.vocaby.application.feature_save.domain.use_cases

import com.vocaby.application.feature_user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSavesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<List<String>> {
        val userId = userRepository.getUser()
        return userRepository.getSavedWordsFlow(userId)
    }

}