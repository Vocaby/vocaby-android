package com.vocaby.application.feature_datatransfer.domain.use_case

import android.net.Uri
import com.vocaby.application.R
import com.vocaby.application.core.util.UiText
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.presentation.DataTransferState
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ImportCustomEntriesUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
    private val dataTransferRepository: DataTransferRepository,
    private val availableTypes: List<Type>
) {
    operator fun invoke(uri: Uri): Flow<DataTransferState> = flow {
        val userId = userRepository.getUser()
        emit(DataTransferState.InProgress(message = UiText(textResource = R.string.data_transfer_reading_import)))

        val entryImportData = dataTransferRepository.importEntriesBackupFromExternalStorage(uri, availableTypes)
        emit(
            DataTransferState.InProgress(
            message = UiText(textResource = R.string.data_transfer_importing),
            count = entryImportData.size
        ))

        customDictionaryRepository.insertNewEntries(userId, entryImportData)
        emit(DataTransferState.Success(message = UiText(textResource = R.string.data_transfer_import_complete)))
    }
}