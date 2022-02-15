package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.states.UserInputState

class ValidateCustomEntryUseCase {
    operator fun invoke(entry: String, customEntries: List<UserEntry>): UserInputState {
        if (entry.isEmpty()) {
            return UserInputState.EmptyInput
        } else if (Formatter.containsSpecialCharacter(entry)) {
            return UserInputState.InvalidInput
        } else {
            if (entry.length > com.vocaby.application.core.Constants.ENTRY_MAX_LENGTH) {
                return UserInputState.LongInput
            } else {
                val index = customEntries.indexOfFirst { it.entry == entry }
                if (index != -1) {
                    return UserInputState.SameInput(index)
                } else {
                    return UserInputState.Valid(entry)
                }
            }
        }
    }
}