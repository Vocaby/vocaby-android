package com.vocaby.application.feature_save.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SaveModel(
    val saveId: Int?,
    val saved: Boolean
): Parcelable
