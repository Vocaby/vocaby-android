package com.vocaby.application.viewmodels

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vocaby.application.R
import com.vocaby.application.data.VocabyRepository
import com.vocaby.application.exceptions.IllegalFileException
import com.vocaby.application.utils.Logger
import com.vocaby.application.utils.SingleLiveEvent
import kotlinx.coroutines.*
import java.io.StreamCorruptedException

class DataTransferViewModel(val repository: VocabyRepository) : ViewModel() {
    companion object {
        const val EXPORT_SAVE = 0
        const val EXPORT_ENTRY = 1
        const val EXPORT_SAVE_BACKUP = 2
        const val EXPORT_ENTRY_BACKUP = 3
        const val IMPORT_SAVE = 4
        const val IMPORT_ENTRY = 5
    }

    private val transferScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var actionType: Int = -1
    private val _transferSuccessful: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val _progressText: SingleLiveEvent<Int> = SingleLiveEvent()
    private val _progressCounter: SingleLiveEvent<Int> = SingleLiveEvent()

    val transferStatus: LiveData<Boolean>
        get() = _transferSuccessful
    val progressText: LiveData<Int>
        get() = _progressText
    val progressCounter: LiveData<Int>
        get() = _progressCounter

    private val importExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _transferSuccessful.postValue(false)
        when (throwable) {
            is IllegalFileException -> {
                when (throwable.code) {
                    IllegalFileException.INVALID_FORMAT -> {
                        _progressText.postValue(R.string.data_transfer_import_error_invalid_format)
                    }
                    IllegalFileException.INVALID_FILE -> {
                        _progressText.postValue(R.string.data_transfer_import_error_invalid_file)
                    }
                    else -> {
                        _progressText.postValue(R.string.data_transfer_import_error_generic)
                    }
                }
            }
            is ClassCastException -> {
                _progressText.postValue(R.string.data_transfer_import_error_invalid_file)
            }
            is StreamCorruptedException -> {
                _progressText.postValue(R.string.data_transfer_import_error_invalid_file)
            }
            else -> {
                _progressText.postValue(R.string.data_transfer_import_error_generic)
            }
        }
    }

    private val exportExceptionHandler = CoroutineExceptionHandler { _, _ ->
        _transferSuccessful.postValue(false)
        _progressText.postValue(R.string.data_transfer_export_empty)
    }

    fun handleReceived(received: Intent): Intent {
        actionType = received.getIntExtra("TYPE", -1)
        val intent: Intent
        when (actionType) {
            EXPORT_SAVE -> {
                intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "my_vocaby_saves.json")
                }
            }

            EXPORT_ENTRY -> {
                intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "my_vocaby_entries.json")
                }
            }

            EXPORT_SAVE_BACKUP -> {
                intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TITLE, "vocaby_saves.backup")
                }
            }

            EXPORT_ENTRY_BACKUP -> {
                intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TITLE, "vocaby_entries.backup")
                }
            }

            IMPORT_SAVE -> {
                intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                }
            }

            IMPORT_ENTRY -> {
                intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                }
            }

            else -> {
                intent = Intent()
            }
        }

        return intent
    }

    fun handleResult(result: Intent?) {
        result?.let {
            if (actionType != -1) {
                val uri = result.data!!
                when (actionType) {
                    EXPORT_SAVE -> writeSaves(uri)
                    EXPORT_ENTRY -> writeEntries(uri)
                    EXPORT_SAVE_BACKUP -> writeSavesForBackup(uri)
                    EXPORT_ENTRY_BACKUP -> writeEntriesForBackup(uri)
                    IMPORT_SAVE -> importSaves(uri)
                    IMPORT_ENTRY -> importEntries(uri)
                }
            }
        }
    }

    fun addResult(): Intent {
        return Intent().putExtra("TYPE", actionType)
    }

    private fun importSaves(uri: Uri) {
        transferScope.launch(importExceptionHandler) {
            _progressText.postValue(R.string.data_transfer_reading_import)
            val saves = repository.importSavesFromExternalStorage(uri)
            _progressText.postValue(R.string.data_transfer_importing)
            _progressCounter.postValue(saves.size)
            repository.addSaveItems(saves)
            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_import_complete)
        }
    }

    private fun importEntries(uri: Uri) {
        transferScope.launch(importExceptionHandler) {
            _progressText.postValue(R.string.data_transfer_reading_import)
            val entryImportData = repository.importEntriesBackupFromExternalStorage(uri)
            _progressText.postValue(R.string.data_transfer_import_setup)
            repository.clearUserEntries()
            _progressText.postValue(R.string.data_transfer_importing)
            _progressCounter.postValue(entryImportData.size)
             repository.insertNewEntries(entryImportData)
            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_import_complete)
        }
    }

    private fun writeEntriesForBackup(uri: Uri) {
        transferScope.launch(exportExceptionHandler) {
            _progressText.postValue(R.string.data_transfer_fetching_entries)
            val entries = repository.getAllUserEntries()
            _progressText.postValue(R.string.data_transfer_exporting_backup)
            repository.writeEntriesBackupToExternalStorage(entries, uri)
            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_export_complete)
        }
    }

    private fun writeSavesForBackup(uri: Uri) {
        transferScope.launch(exportExceptionHandler) {
            val saves = repository.getSavedWords()
            _progressText.postValue(R.string.data_transfer_exporting_backup)
            repository.writeSavesBackupToExternalStorage(saves, uri)
            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_export_complete)
        }
    }

    private fun writeSaves(uri: Uri) {
        _progressText.value = R.string.data_transfer_fetching_data
        transferScope.launch(exportExceptionHandler) {
            val saves = repository.getSavedWords()
            repository.writeSavesToExternalStorage(saves, uri)
            _progressText.postValue(R.string.data_transfer_exporting_saves)
            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_export_complete)
        }
    }

    private fun writeEntries(uri: Uri) {
        _progressText.value = R.string.data_transfer_fetching_data
        transferScope.launch(exportExceptionHandler) {
            _progressText.postValue(R.string.data_transfer_fetching_entries)
            val entries = repository.getAllUserEntries()
            _progressText.postValue(R.string.data_transfer_exporting_backup)
            repository.writeEntriesToExternalStorage(entries, uri)
            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_export_complete)
        }
    }

    fun cancelJob() {
        if (transferScope.isActive) {
            transferScope.cancel("User cancelled the job")
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (transferScope.isActive) {
            transferScope.cancel("Activity cleared")
        }
    }
}

class DataTransferViewModelFactory(
    private val repository: VocabyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataTransferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DataTransferViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
