package com.vocaby.application.feature_save.domain.use_cases.save

import com.vocaby.application.feature_save.domain.use_cases.collection.GetSaveCollectionItemsUseCase
import com.vocaby.application.feature_save.domain.use_cases.collection.RemoveCollectionItemUseCase

class SaveUseCases(
    val getUserSavesUseCase: GetUserSavesUseCase,
    val removeSaveItemUseCase: RemoveSaveItemUseCase,
    val clearUserSavesUseCase: ClearUserSavesUseCase,
    val getSaveCollectionItemsUseCase: GetSaveCollectionItemsUseCase,
    val removeCollectionItemUseCase: RemoveCollectionItemUseCase
)