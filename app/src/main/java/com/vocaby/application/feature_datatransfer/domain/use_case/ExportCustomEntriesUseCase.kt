package com.vocaby.application.feature_datatransfer.domain.use_case

import android.net.Uri
import com.vocaby.application.R
import com.vocaby.application.core.util.UiText
import com.vocaby.application.feature_datatransfer.domain.model.EntryExportModel
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.presentation.DataTransferState
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ExportCustomEntriesUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
    private val dataTransferRepository: DataTransferRepository
) {
    operator fun invoke(uri: Uri): Flow<DataTransferState> = flow {
        emit(DataTransferState.InProgress(message = UiText(textResource = R.string.data_transfer_fetching_data)))
        val userId = userRepository.getUser()
        val entries = customDictionaryRepository.getAllUserEntries(userId)
        if (entries.isEmpty()) {
            emit(DataTransferState.Error(uiText = UiText(textResource = R.string.data_transfer_export_empty)))
        } else {
            emit(DataTransferState.InProgress(
                message = UiText(textResource = R.string.data_transfer_exporting_saves),
                count = entries.size
            ))
            dataTransferRepository.writeEntriesToExternalStorage(EntryExportModel(entries), uri)
            emit(DataTransferState.Success(message = UiText(textResource = R.string.data_transfer_export_complete)))
        }
    }
}