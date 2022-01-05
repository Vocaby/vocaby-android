package com.vocaby.app.payloads

import android.os.Parcelable
import com.vocaby.app.models.customentry.UserEntry
import com.vocaby.app.states.ItemState
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemEntryPayload(val payload: UserEntry, var state: ItemState) : Parcelable
