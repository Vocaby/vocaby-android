package com.vocaby.application.feature_datatransfer.domain.use_case

import android.net.Uri
import com.vocaby.application.R
import com.vocaby.application.core.util.UiText
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.presentation.DataTransferState
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.local.entity.UserSave
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ImportSavesUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val dataTransferRepository: DataTransferRepository
) {
    suspend operator fun invoke(uri: Uri): Flow<DataTransferState> = flow {
        val userId = userRepository.getUser()
        emit(DataTransferState.InProgress(message = UiText(textResource = R.string.data_transfer_reading_import)))

        val saves = dataTransferRepository.importSavesFromExternalStorage(uri)
        emit(DataTransferState.InProgress(
            message = UiText(textResource = R.string.data_transfer_importing),
            count = saves.size
        ))

        val userSaves = mutableListOf<UserSave>()
        for (save in saves) {
//            userSaves.add(UserSave(userId, save))
        }

        // userRepository.clearSaves(userId)
//        userRepository.addSaveItems(userSaves)
        emit(DataTransferState.Success(message = UiText(textResource = R.string.data_transfer_import_complete)))
    }
}