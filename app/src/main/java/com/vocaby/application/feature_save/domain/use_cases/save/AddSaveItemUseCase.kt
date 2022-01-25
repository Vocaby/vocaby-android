package com.vocaby.application.feature_save.domain.use_cases.save

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class AddSaveItemUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(entry: String) {
        val userId = userRepository.getUser()
        val userSave = UserSave(
            userId,
            entry
        )

        saveRepository.addSaveItem(userSave)
    }
}