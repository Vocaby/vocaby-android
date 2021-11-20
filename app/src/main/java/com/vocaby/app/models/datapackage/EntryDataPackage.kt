package com.vocaby.app.models.datapackage

import com.vocaby.app.models.dictionary.EntryModel

class EntryDataPackage(originalEntryData: EntryModel?, customEntryData: EntryModel?) {
    var originalData: EntryModel? = originalEntryData
    var customData: EntryModel? = customEntryData

    val entryData: EntryModel?
        get() = if (customEntryAvailable()) {
            customData
        } else {
            originalData
        }

    fun bothDataAvailable(): Boolean {
        return originalData != null && customData != null
    }

    fun onlyCustomAvailable(): Boolean {
        return originalData == null && customData != null
    }

    private fun customEntryAvailable(): Boolean {
        return customData != null
    }
}