package com.vocaby.application.feature_save.domain.model

import java.util.*

data class UpdateSaveCollectionModel(
    val collectionId: Int,
    val name: String,
    val lastUpdated: Date,
    var saved: Boolean = false
)
