package com.vocaby.application.feature_datatransfer.domain.use_case

import android.net.Uri
import com.vocaby.application.R
import com.vocaby.application.core.util.UiText
import com.vocaby.application.core.util.exceptions.IllegalFileException
import com.vocaby.application.feature_datatransfer.domain.repository.DataTransferRepository
import com.vocaby.application.feature_datatransfer.presentation.DataTransferState
import com.vocaby.application.feature_dictionary_custom.domain.repository.CustomDictionaryRepository
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_save.data.local.entity.SaveCollection
import com.vocaby.application.feature_save.data.local.entity.SaveCollectionItem
import com.vocaby.application.feature_save.data.local.entity.UserSave
import com.vocaby.application.feature_save.domain.repository.SaveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import javax.inject.Inject

class ImportSavesUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val saveRepository: SaveRepository,
    private val customDictionaryRepository: CustomDictionaryRepository,
    private val dataTransferRepository: DataTransferRepository
) {
    suspend operator fun invoke(uri: Uri): Flow<DataTransferState> = flow {
        val userId = userRepository.getUser()
        val newUser = userRepository.createUser()

        emit(DataTransferState.InProgress(message = UiText(textResource = R.string.data_transfer_reading_import)))
        val transferModel = dataTransferRepository.importSavesFromExternalStorage(uri)

        emit(DataTransferState.InProgress(
            message = UiText(text = "Importing save data..."),
            count = transferModel.savedEntries.size
        ))

        val userSaves = mutableListOf<UserSave>()
        for (save in transferModel.savedEntries) {
            yield()
            userSaves.add(UserSave(newUser, save))
        }

        val saveIds = saveRepository.addSaveItems(userSaves)
        if (saveIds.contains(-1)) {
            throw IllegalFileException(
                "Backup file has invalid save data",
                IllegalFileException.INVALID_FILE
            )
        }

        val saveIdMap: Map<String, Long> = transferModel.savedEntries.zip(saveIds).toMap()

        emit(DataTransferState.InProgress(
            message = UiText(text = "Importing collection data..."),
            count = transferModel.savedEntries.size
        ))

        val collectionModels = mutableListOf<SaveCollection>()
        for (collectionName in transferModel.collections.keys) {
            yield()
            collectionModels.add(SaveCollection(newUser, collectionName))
        }

        val collectionIds = saveRepository.addSaveCollections(collectionModels)
        if (collectionIds.contains(-1)) throw IllegalFileException(
            "Backup file has invalid collection data",
            IllegalFileException.INVALID_FILE
        )

        val collectionIdMap: Map<String, Long> = transferModel.collections.keys.zip(collectionIds).toMap()
        val collectionItems = mutableListOf<SaveCollectionItem>()
        transferModel.collections.forEach { item ->
            val collectionId = collectionIdMap[item.key]?.toInt() ?: throw IllegalFileException(
                "Backup file has invalid collection data",
                IllegalFileException.INVALID_FILE
            )

            for (entry in item.value) {
                yield()
                val saveId = saveIdMap[entry]?.toInt() ?: throw IllegalFileException(
                    "Backup file has invalid collection data",
                    IllegalFileException.INVALID_FILE
                )

                collectionItems.add(
                    SaveCollectionItem(
                        saveId,
                        collectionId
                    )
                )
            }
        }
        saveRepository.addSaveCollectionItems(collectionItems)

        userRepository.setCurrentUser(newUser)
        userRepository.replaceOwnership(userId, newUser)
        emit(DataTransferState.Success(message = UiText(textResource = R.string.data_transfer_import_complete)))
    }.flowOn(Dispatchers.Default)
}