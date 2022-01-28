package com.vocaby.application.feature_save.presentation.collection

sealed class CollectionItemsUiEvent {
    object CloseCollectionDialog: CollectionItemsUiEvent()
    data class ShowUpdateDialog(val collectionName: String): CollectionItemsUiEvent()
    data class ShowCollectionAlert(val message: String): CollectionItemsUiEvent()
}
