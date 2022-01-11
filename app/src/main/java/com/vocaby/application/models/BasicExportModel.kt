package com.vocaby.application.models

import com.google.gson.annotations.SerializedName
import com.vocaby.application.Constants.EXPORT_FILE_TYPE_FIELD

class BasicExportModel<T>(
    @SerializedName(value = EXPORT_FILE_TYPE_FIELD)
    private val vocabyExportType: String,
    val data: List<T>
)