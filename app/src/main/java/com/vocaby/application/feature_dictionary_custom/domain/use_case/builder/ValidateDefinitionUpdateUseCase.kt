package com.vocaby.application.feature_dictionary_custom.domain.use_case.builder

import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.states.UserInputState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ValidateDefinitionUpdateUseCase @Inject constructor() {
    suspend operator fun invoke(
        position: Int,
        oldDefinition: String,
        newDefinition: String,
        oldExample: String,
        newExample: String,
        definitionGroup: DefinitionGroupModel
    ): UserInputState = withContext(Dispatchers.Default) {
        if (newDefinition.isEmpty()) {
            UserInputState.EmptyInput
        } else if (oldDefinition == newDefinition && oldExample == newExample) {
            UserInputState.SameInput(Unit)
        } else if (definitionGroup.hasDefinitionExclusive(newDefinition, position)) {
            UserInputState.InvalidInput
        } else {
            UserInputState.Valid(Unit)
        }
    }
}