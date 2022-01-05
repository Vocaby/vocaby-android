package com.vocaby.app.payloads

import android.os.Parcelable
import com.vocaby.app.states.ItemState
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemIntPayload(val payload: Int, var state: ItemState) : Parcelable
