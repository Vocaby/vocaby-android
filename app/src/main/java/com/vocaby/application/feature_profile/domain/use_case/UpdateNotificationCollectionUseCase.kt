package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel

class UpdateNotificationCollectionUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(selected:Int, collections: List<SaveCollectionModel>?) {
        collections?.let {
            userRepository.setNotificationCollection(it[selected].id)
        }
    }
}