package com.vocaby.app.models

import com.vocaby.app.data.entity.CustomEntry
import com.vocaby.app.models.dictionary.EntryModel

data class EntryImportData(val entries: List<CustomEntry>, val entryModels: List<EntryModel>)