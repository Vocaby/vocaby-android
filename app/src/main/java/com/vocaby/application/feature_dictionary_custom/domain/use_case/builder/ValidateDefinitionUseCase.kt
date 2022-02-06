package com.vocaby.application.feature_dictionary_custom.domain.use_case.builder

import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel
import com.vocaby.application.states.UserInputState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ValidateDefinitionUseCase @Inject constructor() {
    suspend operator fun invoke(definition: String, definitionGroup: DefinitionGroupModel): UserInputState = withContext(Dispatchers.Default) {
        if (definition.isEmpty()) {
           UserInputState.EmptyInput
        } else {
            if (definitionGroup.hasDefinition(definition)) {
                UserInputState.InvalidInput
            } else {
               UserInputState.Valid(Unit)
            }
        }
    }
}