package com.vocaby.application.feature_dictionary_custom.domain.use_case.type

import com.vocaby.application.core.util.Validator
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.states.UserInputState
import javax.inject.Inject

class ValidateTypeUseCase @Inject constructor() {
    operator fun invoke(newType: String, types: List<Type>): UserInputState {
        val sanitized = newType.lowercase().trim()
        return if (sanitized.isEmpty()) {
            UserInputState.EmptyInput
        } else if (Validator.containsSpecialCharacter(sanitized)) {
            UserInputState.InvalidInput
        } else {
            val index = types.indexOfFirst { it.type == sanitized }
            if (index != -1) {
                UserInputState.SameInput(index)
            } else {
                UserInputState.Valid(sanitized)
            }
        }
    }
}