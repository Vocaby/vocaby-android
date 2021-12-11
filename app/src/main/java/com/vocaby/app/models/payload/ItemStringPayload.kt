package com.vocaby.app.models.payload

import android.os.Parcelable
import android.os.Parcel
import android.os.Parcelable.Creator

class ItemStringPayload : Parcelable, ItemPayload<String> {
    override var state: Int = PayloadState.UNCHANGED
    override val payload: String

    constructor(payload: String) {
        this.payload = payload
    }

    constructor(state: Int, payload: String) {
        this.state = state
        this.payload = payload
    }

    private constructor(`in`: Parcel) {
        state = `in`.readInt()
        payload = `in`.readString().toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(state)
        parcel.writeString(payload)
    }

    companion object CREATOR : Creator<ItemStringPayload> {
        override fun createFromParcel(parcel: Parcel): ItemStringPayload {
            return ItemStringPayload(parcel)
        }

        override fun newArray(size: Int): Array<ItemStringPayload?> {
            return arrayOfNulls(size)
        }
    }
}