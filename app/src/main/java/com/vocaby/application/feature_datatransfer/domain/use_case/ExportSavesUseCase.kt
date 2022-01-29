package com.vocaby.application.feature_datatransfer.domain.use_case

import android.net.Uri
import com.vocaby.application.R
import com.vocaby.application.core.util.UiText
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.presentation.DataTransferState
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ExportSavesUseCase @Inject constructor(
    val userRepository: UserRepository,
    val dataTransferRepository: DataTransferRepository
) {
    operator fun invoke(uri: Uri): Flow<DataTransferState> = flow {
        emit(DataTransferState.InProgress(message = UiText(textResource = R.string.data_transfer_fetching_data)))
        val userId = userRepository.getUser()
//        val saves = userRepository.getSavedWords(userId)
        val saves = listOf<String>()
        if (saves.isEmpty()) {
            emit(DataTransferState.Error(
                uiText = UiText(textResource = R.string.data_transfer_export_empty)
            ))
        } else {
            emit(DataTransferState.InProgress(
                message = UiText(textResource = R.string.data_transfer_exporting_saves),
                count = saves.size
            ))
            dataTransferRepository.writeSavesToExternalStorage(saves, uri)
            emit(DataTransferState.Success(message = UiText(textResource = R.string.data_transfer_export_complete)))
        }
    }
}