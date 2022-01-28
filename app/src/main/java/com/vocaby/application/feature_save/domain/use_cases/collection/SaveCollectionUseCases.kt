package com.vocaby.application.feature_save.domain.use_cases.collection

class SaveCollectionUseCases(
    val getSaveCollectionsUseCase: GetSaveCollectionsUseCase,
    val addSaveCollectionUseCase: AddSaveCollectionUseCase,
    val removeSaveCollectionUseCase: RemoveSaveCollectionUseCase,
    val updateSaveCollectionUseCase: UpdateSaveCollectionUseCase
)