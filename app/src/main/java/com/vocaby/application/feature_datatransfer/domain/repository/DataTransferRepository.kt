package com.vocaby.application.feature_datatransfer.domain.repository

import android.net.Uri
import com.vocaby.application.feature_datatransfer.domain.model.EntryExportModel
import com.vocaby.application.feature_datatransfer.domain.model.SaveExportModel
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.domain.model.EntryModel

interface DataTransferRepository {
    suspend fun importSavesFromExternalStorage(uri: Uri): List<String>
    suspend fun importEntriesBackupFromExternalStorage(uri: Uri, availableTypes: List<Type>): List<EntryModel>
    suspend fun writeSavesToExternalStorage(exportModel: SaveExportModel, uri: Uri)
    suspend fun writeEntriesToExternalStorage(exportModel: EntryExportModel, uri: Uri)
}