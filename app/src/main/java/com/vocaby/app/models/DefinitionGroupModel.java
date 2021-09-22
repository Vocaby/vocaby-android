package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DefinitionGroupModel implements Parcelable {
    String type;
    List<DefinitionModel> definitionData;

    public DefinitionGroupModel(String type, List<DefinitionModel> definitionData) {
        this.type = type;
        this.definitionData = definitionData;
    }

    protected DefinitionGroupModel(Parcel in) {
        definitionData = new ArrayList<>();
        type = in.readString();
        in.readTypedList(definitionData, DefinitionModel.CREATOR);
    }

    public static final Creator<DefinitionGroupModel> CREATOR = new Creator<DefinitionGroupModel>() {
        @Override
        public DefinitionGroupModel createFromParcel(Parcel in) {
            return new DefinitionGroupModel(in);
        }

        @Override
        public DefinitionGroupModel[] newArray(int size) {
            return new DefinitionGroupModel[size];
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
