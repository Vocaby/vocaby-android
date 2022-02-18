package com.vocaby.application.core.payloads

import android.os.Parcelable
import com.vocaby.application.core.states.ItemState
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemStringPayload(val payload: String, var state: ItemState) : Parcelable
