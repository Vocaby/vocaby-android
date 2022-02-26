package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.core.states.UserInputState
import com.vocaby.application.core.util.Validator
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry

class ValidateCustomEntryUseCase {
    operator fun invoke(entry: String, customEntries: List<UserEntry?>): UserInputState {
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
                    val index = customEntries.indexOfFirst { it?.entry == entry }
                    if (index != -1) {
                        UserInputState.SameInput(index)
                    } else {
                        UserInputState.Valid(entry)
                    }
                }
            }
        }
    }
}