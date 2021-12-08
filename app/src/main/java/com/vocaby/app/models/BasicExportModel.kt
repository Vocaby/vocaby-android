package com.vocaby.app.models

import com.google.gson.annotations.SerializedName
import com.vocaby.app.Constants.EXPORT_FILE_TYPE_FIELD

class BasicExportModel<T>(
    @SerializedName(value = EXPORT_FILE_TYPE_FIELD)
    private val vocabyExportType: String,
    val data: List<T>
)