package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class DefinitionModel implements Parcelable, Comparable<DefinitionModel> {
    private int id;
    private String type;
    private String definition;
    private String example;
    private int order;

    public DefinitionModel(DefinitionModel def) {
        this.id = def.id;
        this.type = def.type;
        this.definition = def.definition;
        this.example = def.example;
        this.order = def.order;
    }

    public DefinitionModel(int id, String type, String definition, String example, int order) {
        this.id = id;
        this.type = type;
        this.definition = definition;
        this.example = example;
        this.order = order;
    }

    public DefinitionModel(String type, String definition, String example, int order) {
        this.id = -1;
        this.type = type;
        this.definition = definition;
        this.example = example;
        this.order = order;
    }

    protected DefinitionModel(Parcel in) {
        id = in.readInt();
        type = in.readString();
        definition = in.readString();
        example = in.readString();
        order = in.readInt();
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(type);
        parcel.writeString(definition);
        parcel.writeString(example);
        parcel.writeInt(order);
    }

    @NonNull
    @Override
    public String toString() {
        return definition;
    }

    @Override
    public int compareTo(DefinitionModel definitionModel) {
        return Integer.compare(order, definitionModel.getOrder());
    }
}
