package com.vocaby.application.feature_datatransfer.domain.use_case

import android.net.Uri
import com.vocaby.application.R
import com.vocaby.application.core.util.UiText
import com.vocaby.application.feature_datatransfer.domain.model.SaveExportModel
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.presentation.DataTransferState
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ExportSavesUseCase @Inject constructor(
    val userRepository: UserRepository,
    val saveRepository: SaveRepository,
    val dataTransferRepository: DataTransferRepository
) {
    operator fun invoke(uri: Uri): Flow<DataTransferState> = flow {
        emit(DataTransferState.InProgress(message = UiText(textResource = R.string.data_transfer_fetching_data)))
        val userId = userRepository.getUser()
        val saves = saveRepository.getAllSavedEntriesFlow(userId).first()

        if (saves.isEmpty()) {
            emit(DataTransferState.Error(
                uiText = UiText(textResource = R.string.data_transfer_export_empty)
            ))
        } else {
            val collections = saveRepository.getSaveCollections(userId).first()
            val collectionMap = mutableMapOf<String, List<String>>()
            for (collection in collections) {
                val collectionItems = saveRepository.getCollectionItems(collection.id)
                collectionMap.put(collection.collectionName, collectionItems)
            }

            emit(DataTransferState.InProgress(
                message = UiText(textResource = R.string.data_transfer_exporting_saves),
                count = saves.size
            ))

            val exportModel = SaveExportModel(
                saves,
                collectionMap
            )
            dataTransferRepository.writeSavesToExternalStorage(exportModel, uri)
            emit(DataTransferState.Success(message = UiText(textResource = R.string.data_transfer_export_complete)))
        }
    }.flowOn(Dispatchers.Default)
}