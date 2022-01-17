package com.vocaby.application.feature_user.presentation.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.vocaby.application.R
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.core.util.exceptions.IllegalFileException
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_user.data.local.entity.UserSave
import com.vocaby.application.feature_user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.StreamCorruptedException
import javax.inject.Inject

@HiltViewModel
class DataTransferViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val customDictionaryRepository: CustomDictionaryRepository
) : ViewModel() {
    companion object {
        const val EXPORT_SAVE_BACKUP = 1
        const val EXPORT_ENTRY_BACKUP = 2
        const val IMPORT_SAVE = 3
        const val IMPORT_ENTRY = 4
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
                _progressText.postValue(R.string.data_transfer_import_error_wrong_backup_file)
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
            EXPORT_SAVE_BACKUP -> {
                intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "my_vocaby_saves.json")
                }
            }

            EXPORT_ENTRY_BACKUP -> {
                intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, "my_vocaby_entries.json")
                }
            }

            IMPORT_SAVE -> {
                intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                }
            }

            IMPORT_ENTRY -> {
                intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
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
                    EXPORT_SAVE_BACKUP -> writeSaves(uri)
                    EXPORT_ENTRY_BACKUP -> writeEntries(uri)
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
            val userId = userRepository.getUser()
            _progressText.postValue(R.string.data_transfer_reading_import)
            val saves = userRepository.importSavesFromExternalStorage(uri)
            _progressText.postValue(R.string.data_transfer_importing)
            _progressCounter.postValue(saves.size)

            val userSaves = mutableListOf<UserSave>()
            for (save in saves) {
                userSaves.add(UserSave(userId, save))
            }
            userRepository.clearSaves(userId)
            userRepository.addSaveItems(userSaves)
            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_import_complete)
        }
    }

    private fun importEntries(uri: Uri) {
        transferScope.launch(importExceptionHandler) {
            val userId = userRepository.getUser()
            _progressText.postValue(R.string.data_transfer_reading_import)
            val entryImportData = userRepository.importEntriesBackupFromExternalStorage(uri)

            _progressText.postValue(R.string.data_transfer_import_setup)
            _progressText.postValue(R.string.data_transfer_importing)
            _progressCounter.postValue(entryImportData.size)
            customDictionaryRepository.insertNewEntries(userId, entryImportData)

            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_import_complete)
        }
    }

    private fun writeSaves(uri: Uri) {
        _progressText.value = R.string.data_transfer_fetching_data
        transferScope.launch(exportExceptionHandler) {
            val userId = userRepository.getUser()
            val saves = userRepository.getSavedWords(userId)
            userRepository.writeSavesToExternalStorage(saves, uri)
            _progressText.postValue(R.string.data_transfer_exporting_saves)
            _transferSuccessful.postValue(true)
            _progressText.postValue(R.string.data_transfer_export_complete)
        }
    }

    private fun writeEntries(uri: Uri) {
        _progressText.value = R.string.data_transfer_fetching_data
        transferScope.launch(exportExceptionHandler) {
            val userId = userRepository.getUser()
            _progressText.postValue(R.string.data_transfer_fetching_entries)
            val entries = customDictionaryRepository.getAllUserEntries(userId)
            _progressText.postValue(R.string.data_transfer_exporting_backup)
            userRepository.writeEntriesToExternalStorage(entries, uri)
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