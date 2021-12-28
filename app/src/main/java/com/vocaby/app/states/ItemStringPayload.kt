package com.vocaby.app.states

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemStringPayload(val payload: String, var state: ItemState) : Parcelable
