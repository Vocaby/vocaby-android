package com.vocaby.application.core.payloads

import android.os.Parcelable
import com.vocaby.application.core.states.ItemState
import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemEntryPayload(val payload: UserEntry, var state: ItemState) : Parcelable
