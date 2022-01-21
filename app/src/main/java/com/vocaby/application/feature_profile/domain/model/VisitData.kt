package com.vocaby.application.feature_profile.domain.model

import android.graphics.Color
import androidx.room.Ignore

data class VisitData(var entry: String, var count: Long) {
    @Ignore
    var color: Int = Color.parseColor("#D1E5D3")
}
