package com.vocaby.application.feature_save.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class UpdateSaveCollectionModel(
    val collectionId: Int,
    val collectionItemId: Int,
    val name: String,
    val lastUpdated: Date,
    var saved: Boolean = false
): Parcelable
