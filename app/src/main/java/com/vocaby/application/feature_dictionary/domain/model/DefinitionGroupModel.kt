package com.vocaby.application.feature_dictionary.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.*

@Parcelize
data class DefinitionGroupModel(
    var type: String,
    var order: Int,
    @Transient
    var groupId: Int = -1,
    var definitionData: MutableList<DefinitionModel> = ArrayList(),
) : Parcelable, Serializable, Comparable<DefinitionGroupModel> {
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
}