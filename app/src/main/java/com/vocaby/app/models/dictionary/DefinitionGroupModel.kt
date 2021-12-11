package com.vocaby.app.models.dictionary;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefinitionGroupModel implements Parcelable, Comparable<DefinitionGroupModel> {
    private int groupId;
    private String type;
    private List<DefinitionModel> definitionData;
    private int order;

    public DefinitionGroupModel(DefinitionGroupModel group) {
        this.groupId = group.groupId;
        this.type = group.type;
        definitionData = new ArrayList<>();
        for (DefinitionModel def : group.getDefinitionData()) {
            definitionData.add(new DefinitionModel(def));
        }
        this.order = group.order;
    }

    public DefinitionGroupModel(int groupId, String type, int order) {
        this.groupId = groupId;
        this.type = type;
        this.definitionData = new ArrayList<>();
        this.order = order;
    }

    public DefinitionGroupModel(String type, int order) {
        this.groupId = -1;
        this.type = type;
        this.definitionData = new ArrayList<>();
        this.order = order;
    }

    public DefinitionGroupModel(String type) {
        this.groupId = -1;
        this.type = type;
        this.definitionData = new ArrayList<>();
        this.order = 0;
    }

    protected DefinitionGroupModel(Parcel in) {
        definitionData = new ArrayList<>();

        groupId = in.readInt();
        type = in.readString();
        in.readTypedList(definitionData, DefinitionModel.CREATOR);
        order = in.readInt();
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

    public void setDefinitionData(List<DefinitionModel> newList) {
        definitionData = newList;
    }

    public DefinitionModel addNewDefinition(String definition, String example) {
        DefinitionModel definitionToAdd = new DefinitionModel(type, definition, example, definitionData.size());
        definitionData.add(definitionToAdd);
        return definitionToAdd;
    }

    public void addNewDefinition(DefinitionModel definitionModel) {
        definitionData.add(definitionModel);
    }

    public boolean hasDefinition(String definition) {
        definition = definition.trim();
        for (DefinitionModel def : definitionData) {
            if (def.getDefinition().equals(definition)) {
                return true;
            }
        }

        return false;
    }

    // TODO: Override list remove
    public DefinitionModel removeDefinition(int position) {
        DefinitionModel definitionToRemove = definitionData.get(position);
        definitionData.remove(position);
        if(position < definitionData.size()) {
            for(int i = position; i < definitionData.size(); i++) {
                definitionData.get(i).setOrder(i);
            }
        }

        return definitionToRemove;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) { this.groupId = groupId; }

    public void setType(String type) {
        this.type = type;
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
        parcel.writeInt(groupId);
        parcel.writeString(type);
        parcel.writeTypedList(definitionData);
        parcel.writeInt(order);
    }

    public boolean isEmpty() {
        return definitionData.isEmpty();
    }

    @Override
    public int compareTo(DefinitionGroupModel otherGroup) {
        return Integer.compare(order, otherGroup.getOrder());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return type.equals(String.valueOf(o));
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
