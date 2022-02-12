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
    val isNew get() = id == -1

    override fun compareTo(other: DefinitionModel): Int {
        return order.compareTo(other.order)
    }
}