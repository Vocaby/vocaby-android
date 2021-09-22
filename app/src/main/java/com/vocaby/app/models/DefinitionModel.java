package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DefinitionModel implements Parcelable {
    private String definition;
    private String type;
    private List<String> examples;

    public DefinitionModel(String definition, String type, List<String> examples) {
        this.definition = definition;
        this.type = type;
        this.examples = examples;
    }

    public DefinitionModel(String definition, String type, String example) {
        this.definition = definition;
        this.type = type;
        this.examples = new ArrayList<>();
        examples.add(example);
    }

    public DefinitionModel(String definition, String type) {
        this.definition = definition;
        this.type = type;
        this.examples = new ArrayList<>();
    }

    protected DefinitionModel(Parcel in) {
        if (examples == null) {
            examples = new ArrayList<>();
        }

        definition = in.readString();
        in.readStringList(examples);
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

    public List<String> getExamples() {
        return examples;
    }

    public String getFirstExample() {
        if (examples.size() == 0) {
            return "";
        } else {
            return examples.get(0);
        }
    }

    public void addExample(String example) {
        examples.add(example);
    }

    public void removeExample(String example) {
        examples.remove(example);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(definition);
        parcel.writeStringList(examples);
    }

    @NonNull
    @Override
    public String toString() {
        return definition;
    }
}
