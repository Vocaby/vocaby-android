package com.vocaby.application.feature_dictionary.domain.model

data class DictionarySearchResult(var originalModel: EntryModel? = null, var customModel: EntryModel? = null) {
    val size:Int get() {
        return if (originalModel != null && customModel != null) 2
        else if (originalModel != null || customModel != null) 1
        else 1
    }

    val data:EntryModel? get() {
        return customModel ?: originalModel
    }

    val entryId:Int get() {
        return originalModel?.id ?: customModel?.id ?: -1
    }
}
