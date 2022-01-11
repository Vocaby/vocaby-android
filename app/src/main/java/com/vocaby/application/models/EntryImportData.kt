package com.vocaby.application.models

import com.vocaby.application.data.entity.CustomEntry
import com.vocaby.application.models.dictionary.EntryModel

data class EntryImportData(val entries: List<CustomEntry>, val entryModels: List<EntryModel>)