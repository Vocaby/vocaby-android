package com.vocaby.application.feature_save.domain.use_cases

class SaveUseCases(
    val getUserSavesUseCase: GetUserSavesUseCase,
    val addSaveItemUseCase: AddSaveItemUseCase,
    val removeSaveItemUseCase: RemoveSaveItemUseCase,
    val clearUserSavesUseCase: ClearUserSavesUseCase
)