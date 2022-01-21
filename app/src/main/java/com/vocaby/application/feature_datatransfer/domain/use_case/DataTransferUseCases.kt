package com.vocaby.application.feature_datatransfer.domain.use_case

class DataTransferUseCases(
    val importSavesUseCase: ImportSavesUseCase,
    val importCustomEntriesUseCase: ImportCustomEntriesUseCase,
    val exportSavesUseCase: ExportSavesUseCase,
    val exportCustomEntriesUseCase: ExportCustomEntriesUseCase
)