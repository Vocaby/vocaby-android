package com.vocaby.application.feature_save.domain.model

import java.util.*

data class SaveCollectionModel(
    val name: String,
    val lastUpdated: Date,
    val count: Int
)
