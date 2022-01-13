package com.vocaby.application.models.dictionary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.*

@Parcelize
class EntryModel(
    @Transient
    var id: Int,
    val entry: String,
    var pronunciation: String?,
    var lastUpdated: String = "",
    var definitionGroups: MutableList<DefinitionGroupModel> = ArrayList(),
) : Parcelable, Serializable {
    constructor(entry: String) : this(-1, entry, null)
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
        get() = definitionGroups[0]

    fun getDefinitionGroup(index: Int): DefinitionGroupModel {
        return definitionGroups[index]
    }

    private fun getGroupIndex(type: String): Int {
        for (i in definitionGroups.indices) {
            if (definitionGroups[i].type == type) return i
        }
        return -1
    }

    override fun toString(): String {
        return "$entry with ${definitionGroups.size} type groups"
    }
}