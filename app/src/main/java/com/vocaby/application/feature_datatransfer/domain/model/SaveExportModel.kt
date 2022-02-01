package com.vocaby.application.feature_datatransfer.domain.model

data class SaveExportModel(
    val savedEntries: List<String>,
    val collections: Map<String, List<String>>
): IExportModel
