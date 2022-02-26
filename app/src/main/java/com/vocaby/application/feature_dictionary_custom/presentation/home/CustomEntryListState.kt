package com.vocaby.application.feature_dictionary_custom.presentation.home

import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import java.util.*


sealed class CustomEntryListState {
    object InProgress: CustomEntryListState()
    data class UpdateEntries(val entries: LinkedList<UserEntry?>): CustomEntryListState()
}
