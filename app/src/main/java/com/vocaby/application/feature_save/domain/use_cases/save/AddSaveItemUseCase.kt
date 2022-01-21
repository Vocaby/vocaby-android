package com.vocaby.application.feature_save.domain.use_cases.save

import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import javax.inject.Inject

class AddSaveItemUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val dictionaryRepository: DictionaryRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
    private val saveRepository: SaveRepository
) {
    suspend operator fun invoke(entry: String) {
        val userId = userRepository.getUser()
        val id = dictionaryRepository.getEntryIdFromDatabase(entry)

        val userSave = id?.let {
            UserSave(
                userId,
                it.toInt(),
                null
            )
        } ?: run {
            val customId = customDictionaryRepository.getUserEntryId(userId, entry)
            UserSave(
                userId,
                null,
                customId
            )
        }

        if (userSave.entryId != null || userSave.customEntryId != null) saveRepository.addSaveItem(userSave)
    }
}