package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.core.states.UserInputState
import com.vocaby.application.core.util.Validator
import com.vocaby.application.feature_dictionary_custom.common.Constants.ENTRY_MAX_LENGTH
import javax.inject.Inject

class ValidateCustomEntryUseCase @Inject constructor() {
    operator fun invoke(entry: String): UserInputState {
        return when {
            entry.isEmpty() -> {
                UserInputState.EmptyInput
            }
            Validator.containsSpecialCharacter(entry) -> {
                UserInputState.InvalidInput
            }
            else -> {
                if (entry.length > ENTRY_MAX_LENGTH) {
                    UserInputState.LongInput
                } else {
                    UserInputState.Valid(entry)
                }
            }
        }
    }
}