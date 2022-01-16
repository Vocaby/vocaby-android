package com.vocaby.application.feature_dictionary.domain.model

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.io.Serializable
import java.util.*

class DefinitionGroupModel : Parcelable, Serializable, Comparable<DefinitionGroupModel> {
    @Transient
    var groupId: Int
    var type: String
    var definitionData: MutableList<DefinitionModel>
    var order: Int

    constructor(group: DefinitionGroupModel) {
        groupId = group.groupId
        type = group.type
        definitionData = ArrayList()

        for (def in group.definitionData) {
            definitionData.add(DefinitionModel(def))
        }

        order = group.order
    }

    constructor(groupId: Int, type: String, order: Int) {
        this.groupId = groupId
        this.type = type
        definitionData = ArrayList()
        this.order = order
    }

    constructor(type: String, order: Int) {
        groupId = -1
        this.type = type
        definitionData = ArrayList()
        this.order = order
    }

    constructor(type: String) {
        groupId = -1
        this.type = type
        definitionData = ArrayList()
        order = 0
    }

    private constructor(`in`: Parcel) {
        definitionData = ArrayList()
        groupId = `in`.readInt()
        type = `in`.readString().toString()
        `in`.readTypedList(definitionData, DefinitionModel)
        order = `in`.readInt()
    }

    fun addNewDefinition(definition: String, example: String?): DefinitionModel {
        val definitionToAdd = DefinitionModel(type, definition, example, definitionData.size)
        definitionData.add(definitionToAdd)
        return definitionToAdd
    }

    fun addNewDefinition(definitionModel: DefinitionModel) {
        definitionData.add(definitionModel)
    }

    fun hasDefinition(definition: String): Boolean {
        val cleanDefinition = definition.trim { it <= ' ' }
        for (def in definitionData) {
            if (def.definition == cleanDefinition) {
                return true
            }
        }

        return false
    }

    fun hasDefinitionExclusive(definition: String, index: Int): Boolean {
        val cleanDefinition = definition.trim { it <= ' ' }
        for ((i, def) in definitionData.withIndex()) {
            if (i == index) continue

            if (def.definition == cleanDefinition) {
                return true
            }
        }

        return false
    }

    // TODO: Override list remove
    fun removeDefinition(position: Int): DefinitionModel {
        val definitionToRemove = definitionData[position]
        definitionData.removeAt(position)
        if (position < definitionData.size) {
            for (i in position until definitionData.size) {
                definitionData[i].order = i
            }
        }
        return definitionToRemove
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(groupId)
        parcel.writeString(type)
        parcel.writeTypedList(definitionData)
        parcel.writeInt(order)
    }

    val isEmpty: Boolean
        get() = definitionData.isEmpty()

    override fun compareTo(other: DefinitionGroupModel): Int {
        return order.compareTo(other.order)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return if (other == null || javaClass != other.javaClass) false else type == other.toString()
    }

    override fun hashCode(): Int {
        return Objects.hash(type)
    }

    companion object CREATOR : Creator<DefinitionGroupModel> {
        override fun createFromParcel(parcel: Parcel): DefinitionGroupModel {
            return DefinitionGroupModel(parcel)
        }

        override fun newArray(size: Int): Array<DefinitionGroupModel?> {
            return arrayOfNulls(size)
        }
    }
}