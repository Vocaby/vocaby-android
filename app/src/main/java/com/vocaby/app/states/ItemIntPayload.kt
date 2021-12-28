package com.vocaby.app.states

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemIntPayload(val payload: Int, var state: ItemState) : Parcelable
