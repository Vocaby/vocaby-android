package com.vocaby.application.payloads

import android.os.Parcelable
import com.vocaby.application.states.ItemState
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemIntPayload(val payload: Int, var state: ItemState) : Parcelable
