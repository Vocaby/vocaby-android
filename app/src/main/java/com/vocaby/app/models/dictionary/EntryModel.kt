package com.vocaby.app.models.dictionary

import android.os.Parcelable
import android.os.Parcel
import android.os.Parcelable.Creator
import java.util.ArrayList

class EntryModel : Parcelable {
    var id: Int
    val entry: String
    var pronunciation: String?
    var definitionGroups: MutableList<DefinitionGroupModel>

    constructor(id: Int, entry: String, pronunciation: String) {
        this.id = id
        this.entry = entry
        this.pronunciation = pronunciation
        definitionGroups = ArrayList()
    }

    constructor(entry: String) {
        id = -1
        this.entry = entry
        pronunciation = ""
        definitionGroups = ArrayList()
    }

    private constructor(`in`: Parcel) {
        definitionGroups = ArrayList()
        id = `in`.readInt()
        entry = `in`.readString().toString()
        pronunciation = `in`.readString()

        `in`.readTypedList(definitionGroups, DefinitionGroupModel)
    }

    val isEmpty: Boolean
        get() = definitionGroups.isEmpty()


    fun addDefinitionGroup(definitionGroupModel: DefinitionGroupModel) {
        definitionGroups.add(definitionGroupModel)
    }

    fun replaceDefinitionGroup(type: String, newGroup: DefinitionGroupModel) {
        val index = getGroupIndex(type)
        definitionGroups[index] = newGroup
    }

    // TODO: Override list remove
    fun removeGroup(position: Int): DefinitionGroupModel {
        val groupToRemove = definitionGroups[position]
        definitionGroups.removeAt(position)
        if (position < definitionGroups.size) {
            for (i in position until definitionGroups.size) {
                definitionGroups[i].order = i
            }
        }
        return groupToRemove
    }

    fun addDefinition(type: String, definition: String, example: String?) {
        val index = getGroupIndex(type)
        if (index != -1) {
            val group = definitionGroups[index]
            group.addNewDefinition(definition, example)
        } else {
            val newGroup = DefinitionGroupModel(type, definitionGroups.size)
            newGroup.addNewDefinition(definition, example)
            definitionGroups.add(newGroup)
        }
    }

    val firstGroup: DefinitionGroupModel
        get() =definitionGroups[0]

    fun getDefinitionGroup(index: Int): DefinitionGroupModel {
        return definitionGroups[index]
    }

    private fun getGroupIndex(type: String): Int {
        for (i in definitionGroups.indices) {
            if (definitionGroups[i].type == type) return i
        }
        return -1
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(id)
        parcel.writeString(entry)
        parcel.writeString(pronunciation)
        parcel.writeTypedList(definitionGroups)
    }

    companion object CREATOR : Creator<EntryModel> {
        override fun createFromParcel(parcel: Parcel): EntryModel {
            return EntryModel(parcel)
        }

        override fun newArray(size: Int): Array<EntryModel?> {
            return arrayOfNulls(size)
        }
    }
}