package com.vocaby.application.feature_datatransfer.domain.use_case

import android.net.Uri
import com.vocaby.application.R
import com.vocaby.application.core.util.UiText
import com.vocaby.application.feature_datatransfer.common.Constants.GRACE_PERIOD
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.presentation.DataTransferState
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ImportCustomEntriesUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
    private val dataTransferRepository: DataTransferRepository
) {
    operator fun invoke(uri: Uri): Flow<DataTransferState> = flow {
        val userId = userRepository.getUser()
        val newUser = userRepository.createUser()

        emit(DataTransferState.InProgress(message = UiText(textResource = R.string.data_transfer_reading_import)))
        val entryImportData = dataTransferRepository.importEntriesBackupFromExternalStorage(uri)

        emit(
            DataTransferState.InProgress(
                message = UiText(textResource = R.string.data_transfer_importing),
                count = entryImportData.size
            )
        )

        customDictionaryRepository.insertNewEntries(newUser, entryImportData)

        emit(
            DataTransferState.InProgress(
                message = UiText(text = "Finalizing..."),
                count = entryImportData.size
            )
        )
        delay(GRACE_PERIOD)

        customDictionaryRepository.clearUserEntries(userId)
        customDictionaryRepository.replaceUser(userId)
        emit(DataTransferState.Success(message = UiText(textResource = R.string.data_transfer_import_complete)))
    }
}