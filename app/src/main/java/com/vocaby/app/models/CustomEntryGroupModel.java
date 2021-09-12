package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CustomEntryGroupModel implements Parcelable {
    String type;
    List<DefinitionModel> definitionData;

    public CustomEntryGroupModel(String type, List<DefinitionModel> definitionData) {
        this.type = type;
        this.definitionData = definitionData;
    }

    protected CustomEntryGroupModel(Parcel in) {
        definitionData = new ArrayList<>();
        type = in.readString();
        in.readTypedList(definitionData, DefinitionModel.CREATOR);
    }

    public static final Creator<CustomEntryGroupModel> CREATOR = new Creator<CustomEntryGroupModel>() {
        @Override
        public CustomEntryGroupModel createFromParcel(Parcel in) {
            return new CustomEntryGroupModel(in);
        }

        @Override
        public CustomEntryGroupModel[] newArray(int size) {
            return new CustomEntryGroupModel[size];
        }
    };

    public String getType() {
        return type;
    }

    public List<DefinitionModel> getDefinitionData() {
        return definitionData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(type);
        parcel.writeTypedList(definitionData);
    }

    public boolean isEmpty() {
        return definitionData.isEmpty();
    }
}
