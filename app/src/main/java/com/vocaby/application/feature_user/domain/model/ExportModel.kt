package com.vocaby.application.feature_user.domain.model

data class ExportModel<T>(val vocabyExportType: String, val data: List<T>)
