package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.core.states.UserInputState
import com.vocaby.application.core.util.Validator

class ValidateCustomEntryUseCase {
    operator fun invoke(entry: String): UserInputState {
        when {
            entry.isEmpty() -> {
                return UserInputState.EmptyInput
            }
            Validator.containsSpecialCharacter(entry) -> {
                return UserInputState.InvalidInput
            }
            else -> {
                return if (entry.length > com.vocaby.application.core.Constants.ENTRY_MAX_LENGTH) {
                    UserInputState.LongInput
                } else {
                    UserInputState.Valid(entry)
                }
            }
        }
    }
}