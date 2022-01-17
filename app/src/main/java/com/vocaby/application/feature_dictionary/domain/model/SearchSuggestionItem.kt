package com.vocaby.application.feature_dictionary.domain.model

import android.os.Parcel
import android.os.Parcelable.Creator
import com.vocaby.searchview.suggestions.model.SearchSuggestion

class SearchSuggestionItem : SearchSuggestion {
    private val entry: String

    constructor(entry: String) {
        this.entry = entry
    }

    private constructor(`in`: Parcel) {
        entry = `in`.readString()!!
    }

    override val body: String
        get() = entry

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(entry)
    }

    companion object CREATOR : Creator<SearchSuggestionItem> {
        override fun createFromParcel(parcel: Parcel): SearchSuggestionItem {
            return SearchSuggestionItem(parcel)
        }

        override fun newArray(size: Int): Array<SearchSuggestionItem?> {
            return arrayOfNulls(size)
        }
    }
}