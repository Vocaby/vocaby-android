package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.core.util.Formatter
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.states.UserInputState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ValidateCustomEntryUseCase {
    suspend operator fun invoke(entry: String, customEntries: List<UserEntry>): UserInputState = withContext(Dispatchers.Default) {
        if (entry.isEmpty()) {
            UserInputState.EmptyInput
        } else if (Formatter.containsSpecialCharacter(entry)) {
            UserInputState.InvalidInput
        } else {
            if (entry.length > com.vocaby.application.core.Constants.ENTRY_MAX_LENGTH) {
                UserInputState.LongInput
            } else {
                val index = customEntries.indexOfFirst { it.entry == entry }
                if (index != -1) {
                    UserInputState.SameInput(index)
                } else {
                    UserInputState.Valid(entry)
                }
            }
        }
    }
}