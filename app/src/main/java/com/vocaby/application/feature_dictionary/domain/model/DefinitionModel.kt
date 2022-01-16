package com.vocaby.application.feature_dictionary.domain.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.io.Serializable

class DefinitionModel : Parcelable, Serializable, Comparable<DefinitionModel> {
    @Transient
    var id: Int
    @Transient
    var type: String
    var definition: String
    var example: String?
    var order: Int

    constructor(def: DefinitionModel) {
        id = def.id
        type = def.type
        definition = def.definition
        example = def.example
        order = def.order
    }

    constructor(id: Int, type: String, definition: String, example: String?, order: Int) {
        this.id = id
        this.type = type
        this.definition = definition
        this.example = example
        this.order = order
    }

    constructor(type: String, definition: String, example: String?, order: Int) {
        id = -1
        this.type = type
        this.definition = definition
        this.example = example
        this.order = order
    }

    private constructor(`in`: Parcel) {
        id = `in`.readInt()
        type = `in`.readString().toString()
        definition = `in`.readString().toString()
        example = `in`.readString()
        order = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id)
        parcel.writeString(type)
        parcel.writeString(definition)
        parcel.writeString(example)
        parcel.writeInt(order)
    }

    override fun toString(): String {
        return definition
    }

    override fun compareTo(other: DefinitionModel): Int {
        return order.compareTo(other.order)
    }

    companion object CREATOR : Creator<DefinitionModel> {
        override fun createFromParcel(parcel: Parcel): DefinitionModel {
            return DefinitionModel(parcel)
        }

        override fun newArray(size: Int): Array<DefinitionModel?> {
            return arrayOfNulls(size)
        }
    }
}