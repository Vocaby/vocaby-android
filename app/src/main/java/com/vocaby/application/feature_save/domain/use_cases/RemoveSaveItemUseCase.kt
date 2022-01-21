package com.vocaby.application.feature_save.domain.use_cases

import com.vocaby.application.feature_user.domain.repository.UserRepository

class RemoveSaveItemUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(entry: String) {
        val userId = userRepository.getUser()
        userRepository.removeSaveItem(userId, entry)
    }
}