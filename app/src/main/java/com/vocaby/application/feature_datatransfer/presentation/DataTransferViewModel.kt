package com.vocaby.application.feature_datatransfer.presentation

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.vocaby.application.R
import com.vocaby.application.core.util.UiText
import com.vocaby.application.core.util.exceptions.IllegalFileException
import com.vocaby.application.feature_datatransfer.domain.use_case.DataTransferUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.StreamCorruptedException
import javax.inject.Inject

@HiltViewModel
class DataTransferViewModel @Inject constructor(
    private val dataTransferUseCases: DataTransferUseCases
) : ViewModel() {
    companion object {
        const val EXPORT_SAVE_BACKUP = 1
        const val EXPORT_ENTRY_BACKUP = 2
        const val IMPORT_SAVE = 3
        const val IMPORT_ENTRY = 4
    }

    private val transferScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var actionType: Int = -1
    private val _transferState = MutableStateFlow<DataTransferState>(DataTransferState.InProgress())

    val transferState get() = _transferState.asSharedFlow()

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
                    type = "*/*"
                    val mimeTypes = arrayOf("application/*", "text/*")
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }
            }

            IMPORT_ENTRY -> {
                intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    val mimeTypes = arrayOf("application/*", "text/*")
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
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
        transferScope.launch {
            dataTransferUseCases.importSavesUseCase(uri)
                .catch { e -> emit(catchImportExceptions(e)) }
                .collectLatest { transferState ->
                    _transferState.emit(transferState)
                }
        }
    }

    private fun importEntries(uri: Uri) {
        transferScope.launch {
            dataTransferUseCases.importCustomEntriesUseCase(uri)
                .catch { e -> emit(catchImportExceptions(e)) }
                .collectLatest { transferState ->
                    _transferState.emit(transferState)
                }
        }
    }

    private fun writeSaves(uri: Uri) {
        transferScope.launch {
            transferScope.launch {
                dataTransferUseCases.exportSavesUseCase(uri)
                    .collectLatest { transferState ->
                        _transferState.emit(transferState)
                    }
            }
        }
    }

    private fun writeEntries(uri: Uri) {
        transferScope.launch {
            dataTransferUseCases.exportCustomEntriesUseCase(uri)
                .collectLatest { transferState ->
                    _transferState.emit(transferState)
                }
        }
    }

    fun cancelJob() {
        if (transferScope.isActive) {
            transferScope.cancel("User cancelled the job")
        }
    }

    private fun catchImportExceptions(e: Throwable): DataTransferState {
        return when (e) {
            is IllegalFileException -> {
                when (e.code) {
                    IllegalFileException.INVALID_FORMAT -> {
                        DataTransferState.Error(uiText = UiText(textResource = R.string.data_transfer_import_error_invalid_format))
                    }
                    IllegalFileException.INVALID_FILE -> {
                        DataTransferState.Error(uiText = UiText(textResource = R.string.data_transfer_import_error_invalid_file))
                    }
                    else -> {
                        DataTransferState.Error(uiText = UiText(textResource = R.string.data_transfer_import_error_generic))
                    }
                }
            }
            is StreamCorruptedException -> {
                DataTransferState.Error(uiText = UiText(textResource = R.string.data_transfer_import_error_invalid_file))
            }
            is NullPointerException -> {
                DataTransferState.Error(uiText = UiText(textResource = R.string.data_transfer_import_error_wrong_backup_file))
            }
            else -> {
                DataTransferState.Error(uiText = UiText(textResource = R.string.data_transfer_import_error_generic))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (transferScope.isActive) {
            transferScope.cancel("Activity cleared")
        }
    }
}