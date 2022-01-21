package com.vocaby.application.feature_datatransfer.domain.repository

import android.net.Uri
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.domain.model.EntryModel

interface DataTransferRepository {
    fun importSavesFromExternalStorage(uri: Uri): List<String>
    fun importEntriesBackupFromExternalStorage(uri: Uri, availableTypes: List<Type>): List<EntryModel>
    fun writeSavesToExternalStorage(saves: List<String>, uri: Uri)
    fun writeEntriesToExternalStorage(entries: List<EntryModel>, uri: Uri)
}