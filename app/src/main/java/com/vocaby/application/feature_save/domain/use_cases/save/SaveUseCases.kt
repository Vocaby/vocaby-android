package com.vocaby.application.feature_save.domain.use_cases.save

class SaveUseCases(
    val getUserSavesUseCase: GetUserSavesUseCase,
    val removeSaveItemUseCase: RemoveSaveItemUseCase,
    val clearUserSavesUseCase: ClearUserSavesUseCase
)