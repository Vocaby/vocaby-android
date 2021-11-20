package com.vocaby.app.viewmodels

import android.view.View.GONE
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocaby.app.R
import com.vocaby.app.data.VocabyRepositoryKt
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.models.viewstate.SaveStateModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchResultsViewModel(
    private val entry: String,
    private val repository: VocabyRepositoryKt,
    private val saveModel: SaveStateModel,
): ViewModel() {
    private var _entryData: MutableLiveData<ArrayList<EntryModel?>> = MutableLiveData()
    private var _saveState: MutableLiveData<SaveStateModel> = MutableLiveData()
    private var _missingDictionary: MutableLiveData<Int> = MutableLiveData()

    val entryData: MutableLiveData<ArrayList<EntryModel?>> get() = _entryData
    val saveState: MutableLiveData<SaveStateModel> get() = _saveState
    val missingDictionary: MutableLiveData<Int> get() = _missingDictionary

    init {
        saveModel.enabled = false
        _saveState.postValue(saveModel)

        viewModelScope.launch {
            repository.hasSaved(entry).collect {
                saveModel.enabled = true
                saveModel.saved = it != 0
                _saveState.postValue(saveModel)
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            saveModel.enabled = false
            _saveState.postValue(saveModel)

            val wordPackage = repository.getEntryPackage(entry)
            val data = ArrayList<EntryModel?>()

            if (wordPackage.bothDataAvailable()) {
                data.add(wordPackage.customData)
                data.add(wordPackage.originalData)
            } else if (wordPackage.onlyCustomAvailable()) {
                data.add(wordPackage.customData)
                _missingDictionary.postValue(R.id.selection_original)
            } else {
                data.add(wordPackage.originalData)
                _missingDictionary.postValue(R.id.selection_custom)

                // NO DEFINITION FOUND
                if (wordPackage.originalData == null) {
                    saveModel.visibility = GONE
                    _saveState.postValue(saveModel)
                }
            }

            _entryData.postValue(data)
        }
    }
}

class SearchResultsViewModelFactory(
    private val entry: String,
    private val repository: VocabyRepositoryKt,
    private val saveModel: SaveStateModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchResultsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchResultsViewModel(entry, repository, saveModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}