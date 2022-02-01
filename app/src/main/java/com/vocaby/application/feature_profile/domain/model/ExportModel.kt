package com.vocaby.application.feature_profile.domain.model

import com.vocaby.application.feature_datatransfer.domain.model.IExportModel

data class ExportModel(val vocabyExportType: String, val exportModel: IExportModel)
