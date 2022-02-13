package com.vocaby.application.feature_profile.domain.use_case

import com.vocaby.application.feature_profile.domain.model.ProfileModel
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileDataUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: Int): Flow<ProfileModel?> {
        return userRepository.getProfileData(userId)
    }
}