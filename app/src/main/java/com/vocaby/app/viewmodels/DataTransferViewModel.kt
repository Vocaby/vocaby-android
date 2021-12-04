package com.vocaby.app.viewmodels

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocaby.app.R
import com.vocaby.app.data.VocabyRepositoryKt
import com.vocaby.app.utils.Logger
import com.vocaby.app.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class DataTransferViewModel(val repository: VocabyRepositoryKt) : ViewModel() {
    companion object {
        const val EXPORT_SAVE = 0
        const val EXPORT_SAVE_BACKUP = 1
        const val IMPORT_SAVE = 2
        const val IMPORT_ENTRY = 3
    }

    private val mTransferSuccessful: SingleLiveEvent<Boolean> = SingleLiveEvent()
    private val mProgressText: SingleLiveEvent<Int> = SingleLiveEvent()
    private var actionType: Int = -1

    val transferStatus: LiveData<Boolean>
        get() = mTransferSuccessful
    val progressText: LiveData<Int>
        get() = mProgressText


    fun handleReceived(received: Intent): Intent {
        actionType = received.getIntExtra("TYPE", -1)
        val intent: Intent

        when (actionType) {
            EXPORT_SAVE -> {
                intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TITLE, "vocaby_saves.txt")
                intent.addCategory(Intent.CATEGORY_OPENABLE)
            }

            EXPORT_SAVE_BACKUP -> {
                intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.type = "application/json"
                intent.putExtra(Intent.EXTRA_TITLE, "vocaby_saves_backup.json")
                intent.addCategory(Intent.CATEGORY_OPENABLE)
            }

            IMPORT_SAVE -> {
                intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/json"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
            }

            else -> {
                intent = Intent()
            }
        }

        return intent
    }

    fun handleResult(result: Intent?) {
        if (result != null && actionType != -1) {
            val uri = result.data!!
            when (actionType) {
                EXPORT_SAVE -> writeSaves(uri)
                EXPORT_SAVE_BACKUP -> writeSavesForBackup(uri)
                IMPORT_SAVE -> importSaves(uri)
                else -> Logger.reportErrorToDebug(Throwable("How did I get here"))
            }
        }
    }

    fun addResult(): Intent {
        return Intent().putExtra("TYPE", actionType)
    }

    private fun importSaves(uri: Uri) {
        viewModelScope.launch {
            repository.importSavesFromExternalStorage(uri)
            mTransferSuccessful.value = true
            mProgressText.setValue(R.string.data_transfer_import_complete)

//            mTransferSuccessful.value = false
//            if (error is IllegalFileException) {
//                val resultCode = error.code
//                if (resultCode == IllegalFileException.INVALID_FORMAT) {
//                    mProgressText.setValue(R.string.data_transfer_import_error_invalid_format)
//                } else if (resultCode == IllegalFileException.INVALID_FILE) {
//                    mProgressText.value = R.string.data_transfer_import_error_invalid_file
//                }
//            } else {
//                mProgressText.value = R.string.data_transfer_import_error_generic
//                Logger.reportToDebug(error.message)
//                Logger.reportToDebug(error.javaClass.toString())
//            }
        }
    }

    private fun writeSavesForBackup(uri: Uri) {
        viewModelScope.launch {
            val saves = repository.getSavedWords()
            mProgressText.value = R.string.data_transfer_exporting_backup
            repository.writeSavesJsonToExternalStorage(saves, uri)

            mTransferSuccessful.value = true
            mProgressText.setValue(R.string.data_transfer_export_complete)

//            mTransferSuccessful.value = false
//            mProgressText.value = R.string.data_transfer_export_empty
        }
    }

    private fun writeSaves(uri: Uri) {
        mProgressText.value = R.string.data_transfer_fetching_data
        viewModelScope.launch {
            val saves = repository.getSavedWords()
            mProgressText.value = R.string.data_transfer_exporting_saves
            repository.writeSavesToExternalStorage(saves, uri)
            mTransferSuccessful.value = true
            mProgressText.setValue(R.string.data_transfer_export_complete)

//            mTransferSuccessful.value = false
//            mProgressText.value = R.string.data_transfer_export_empty
        }
    }
}
class DataTransferViewModelFactory(
    private val repository: VocabyRepositoryKt
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DataTransferViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DataTransferViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
