package com.vocaby.application.core.states

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ItemState: Parcelable {
    ADD, DELETE, UPDATE, NOTHING
}
