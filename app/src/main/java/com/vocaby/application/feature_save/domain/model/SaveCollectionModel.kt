package com.vocaby.application.feature_save.domain.model

import com.vocaby.application.feature_profile.common.Constants.ALL_SAVES_COLLECTION_NAME
import java.util.*

data class SaveCollectionModel(
    val id: Int = -1,
    val collectionName: String = ALL_SAVES_COLLECTION_NAME,
    val lastUpdated: Date = Date(),
    val count: Int = 0
)
