package com.vocaby.application.feature_datatransfer.domain.model

import com.vocaby.application.feature_dictionary.domain.model.EntryModel

data class EntryTransferModel(
    val customEntries: List<EntryModel>
): ITransferModel
