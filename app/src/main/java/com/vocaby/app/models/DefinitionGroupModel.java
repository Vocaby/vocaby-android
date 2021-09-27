package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class DefinitionGroupModel implements Parcelable, Comparable<DefinitionGroupModel> {
    private int groupId;
    private String type;
    private List<DefinitionModel> definitionData;
    private int order;

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

    public DefinitionModel addDefinition(String definition, String example) {
        DefinitionModel definitionToAdd = new DefinitionModel(type, definition, example, definitionData.size());
        definitionData.add(definitionToAdd);
        return definitionToAdd;
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

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

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
}
