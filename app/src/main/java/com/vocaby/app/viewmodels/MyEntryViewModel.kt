package com.vocaby.app.viewmodels

import com.vocaby.app.utils.SingleLiveEvent
import com.vocaby.app.models.payload.ItemIntPayload
import android.content.Intent
import com.vocaby.app.models.payload.ItemStringPayload
import com.vocaby.app.models.payload.PayloadState
import android.app.Activity
import androidx.activity.result.ActivityResult
import androidx.lifecycle.*
import com.vocaby.app.Constants
import com.vocaby.app.data.VocabyRepositoryKt
import kotlinx.coroutines.launch
import java.util.ArrayList

class MyEntryViewModel(private val repository: VocabyRepositoryKt): ViewModel() {
    private val _entryCount: MutableLiveData<Int> = MutableLiveData(0)
    private val _entries: SingleLiveEvent<List<String>> = SingleLiveEvent()
    private val _entryState: SingleLiveEvent<ItemIntPayload> = SingleLiveEvent()

    var selectedPosition: Int = -1
    private var customEntries: MutableList<String> = ArrayList()

    val entries: LiveData<List<String>>
        get() = _entries
    val customEntryCount: LiveData<Int>
        get() = _entryCount
    val entryState: LiveData<ItemIntPayload>
        get() = _entryState

    init {
        // populate user entries
        viewModelScope.launch {
            customEntries = repository.getUserEntries() as MutableList<String>
            _entries.postValue(customEntries)
            _entryCount.postValue(customEntries.size)
        }
    }

    fun clearEntries() {
        viewModelScope.launch {
            repository.clearUserEntries()
            customEntries = ArrayList()
            _entries.value = customEntries
            _entryCount.value = 0
        }
    }

    fun addEntryDataToIntent(intent: Intent, entry: String, position: Int): Intent {
        val itemStringPayload = ItemStringPayload(entry)
        selectedPosition = if (position == -1) {
            customEntries.indexOf(entry)
        } else {
            position
        }

        if (selectedPosition == -1) {
            itemStringPayload.state = PayloadState.ADD
        } else {
            itemStringPayload.state = PayloadState.UPDATE
        }

        intent.putExtra(Constants.ITEM_PAYLOAD_KEY, itemStringPayload)
        return intent
    }

    fun handleResult(result: ActivityResult) {
        if (result.data != null && result.resultCode == Activity.RESULT_OK) {
            val receivedPayload: ItemStringPayload? =
                result.data!!.getParcelableExtra(Constants.ITEM_PAYLOAD_KEY)

            receivedPayload?.let {
                if (receivedPayload.state == PayloadState.ADD) {
                    customEntries.add(0, receivedPayload.payload)
                    _entryState.value = ItemIntPayload(ItemIntPayload.ADD, 0)
                } else if (receivedPayload.state == PayloadState.DELETE && selectedPosition != -1) {
                    customEntries.removeAt(selectedPosition)
                    _entryState.value = ItemIntPayload(ItemIntPayload.DELETE, selectedPosition)
                }

                _entryCount.value = customEntries.size
            }
        }
    }

    fun removeCustomEntry(entry: String, position: Int) {
        viewModelScope.launch {
            repository.removeCustomEntry(entry)
            customEntries.removeAt(position)
            _entryCount.postValue(customEntries.size)
            _entryState.value = ItemIntPayload(ItemIntPayload.DELETE, position)
        }
    }
}

class MyEntryViewModelFactory(
    private val repository: VocabyRepositoryKt
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyEntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}