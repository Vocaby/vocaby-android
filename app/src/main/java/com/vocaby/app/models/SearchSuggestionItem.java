package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class SearchSuggestionItem implements SearchSuggestion {
    private final String entry;

    public SearchSuggestionItem(String entry) {
        this.entry = entry;
    }

    public static final Parcelable.Creator<SearchSuggestionItem> CREATOR
            = new Parcelable.Creator<SearchSuggestionItem>() {
        public SearchSuggestionItem createFromParcel(Parcel in) {
            return new SearchSuggestionItem(in);
        }

        public SearchSuggestionItem[] newArray(int size) {
            return new SearchSuggestionItem[size];
        }
    };

    private SearchSuggestionItem(Parcel in) {
        this.entry = in.readString();
    }

    @Override
    public String getBody() {
        return this.entry;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(entry);
    }
}
