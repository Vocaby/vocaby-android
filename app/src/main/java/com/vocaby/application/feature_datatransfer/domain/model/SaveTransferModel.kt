package com.vocaby.application.feature_datatransfer.domain.model

import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.model.CollectionItemModel

data class SaveTransferModel(
    val savedEntries: List<UserSave>,
    val collections: Map<String, List<CollectionItemModel>>
): ITransferModel
