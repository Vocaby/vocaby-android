package com.vocaby.application.feature_profile.domain.model

data class ExportModel<T>(val vocabyExportType: String, val data: List<T>)
