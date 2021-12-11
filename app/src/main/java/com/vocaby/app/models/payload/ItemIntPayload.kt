package com.vocaby.app.models.payload

import android.os.Parcelable
import android.os.Parcel
import android.os.Parcelable.Creator

class ItemIntPayload : Parcelable, ItemPayload<Int?> {
    override var state: Int = PayloadState.UNCHANGED
    override val payload: Int

    constructor(state: Int, payload: Int) {
        this.state = state
        this.payload = payload
    }

    private constructor(`in`: Parcel) {
        state = `in`.readInt()
        payload = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(state)
        parcel.writeInt(payload)
    }

    companion object CREATOR : Creator<ItemIntPayload> {
        override fun createFromParcel(parcel: Parcel): ItemIntPayload {
            return ItemIntPayload(parcel)
        }

        override fun newArray(size: Int): Array<ItemIntPayload?> {
            return arrayOfNulls(size)
        }
    }
}