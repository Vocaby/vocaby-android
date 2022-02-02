package com.vocaby.application.feature_datatransfer.domain.repository

import android.net.Uri
import com.vocaby.application.feature_datatransfer.domain.model.EntryTransferModel
import com.vocaby.application.feature_datatransfer.domain.model.SaveTransferModel
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.domain.model.EntryModel

interface DataTransferRepository {
    suspend fun importSavesFromExternalStorage(uri: Uri): SaveTransferModel
    suspend fun importEntriesBackupFromExternalStorage(uri: Uri, availableTypes: List<Type>): List<EntryModel>
    suspend fun writeSavesToExternalStorage(transferModel: SaveTransferModel, uri: Uri)
    suspend fun writeEntriesToExternalStorage(exportModel: EntryTransferModel, uri: Uri)
}