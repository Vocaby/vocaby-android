package com.vocaby.application.feature_dictionary.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class DefinitionModel(
    @Transient
    var type: String,
    var definition: String,
    var example: String?,
    var order: Int,
    @Transient
    var id: Int = -1,
) : Parcelable, Serializable, Comparable<DefinitionModel> {
    constructor(def: DefinitionModel): this(def.type, def.definition, def.example, def.order, def.id)

    override fun compareTo(other: DefinitionModel): Int {
        return order.compareTo(other.order)
    }
}