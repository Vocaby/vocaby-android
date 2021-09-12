package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class DefinitionModel implements Parcelable {
    private String definition;
    private String example;

    public DefinitionModel(String definition, String example) {
        this.definition = definition;
        this.example = example;
    }

    public DefinitionModel(String definition) {
        this.definition = definition;
        this.example = "";
    }

    protected DefinitionModel(Parcel in) {
        definition = in.readString();
        example = in.readString();
    }

    public static final Creator<DefinitionModel> CREATOR = new Creator<DefinitionModel>() {
        @Override
        public DefinitionModel createFromParcel(Parcel in) {
            return new DefinitionModel(in);
        }

        @Override
        public DefinitionModel[] newArray(int size) {
            return new DefinitionModel[size];
        }
    };

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(definition);
        parcel.writeString(example);
    }
}
