package com.vocaby.application.feature_datatransfer.domain.model

import com.vocaby.application.feature_dictionary.domain.model.EntryModel

data class EntryExportModel(
    val customEntries: List<EntryModel>
): IExportModel
