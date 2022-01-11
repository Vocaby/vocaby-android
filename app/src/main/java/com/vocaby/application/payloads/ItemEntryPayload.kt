package com.vocaby.application.payloads

import android.os.Parcelable
import com.vocaby.application.models.customentry.UserEntry
import com.vocaby.application.states.ItemState
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemEntryPayload(val payload: UserEntry, var state: ItemState) : Parcelable
