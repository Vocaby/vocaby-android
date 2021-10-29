package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class EntryModel implements Parcelable {
    private int id;
    private final String entry;
    private String pronunciation;
    // TODO: Change List to Hashmap?
    private List<DefinitionGroupModel> definitionGroups;

    public EntryModel(int id, String entry) {
        this.id = id;
        this.entry = entry;
        pronunciation = "";
        definitionGroups = new ArrayList<>();
    }

    public EntryModel(String entry) {
        id = -1;
        this.entry = entry;
        pronunciation = "";
        definitionGroups = new ArrayList<>();
    }

    protected EntryModel(Parcel in) {
        definitionGroups = new ArrayList<>();

        id = in.readInt();
        entry = in.readString();
        pronunciation = in.readString();
        in.readTypedList(definitionGroups, DefinitionGroupModel.CREATOR);
    }

    public static final Creator<EntryModel> CREATOR = new Creator<EntryModel>() {
        @Override
        public EntryModel createFromParcel(Parcel in) {
            return new EntryModel(in);
        }

        @Override
        public EntryModel[] newArray(int size) {
            return new EntryModel[size];
        }
    };

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getPronunciation() { return this.pronunciation; }

    public String getEntry() { return entry; }

    public boolean isEmpty() {
        return definitionGroups.isEmpty();
    }

    public void setDefinitionGroups(List<DefinitionGroupModel> newGroups) {
        definitionGroups = newGroups;
    }

    public void addDefinitionGroup(String type) {
       definitionGroups.add(new DefinitionGroupModel(type, definitionGroups.size()));
    }

    public void addDefinitionGroup(DefinitionGroupModel definitionGroupModel) {
        definitionGroups.add(definitionGroupModel);
    }

    public void replaceDefinitionGroup(String type, DefinitionGroupModel newGroup) {
        int index = getGroupIndex(type);
        definitionGroups.set(index, newGroup);
    }

    // TODO: Override list remove
    public DefinitionGroupModel removeGroup(int position) {
        DefinitionGroupModel groupToRemove = definitionGroups.get(position);
        definitionGroups.remove(position);
        if(position < definitionGroups.size()) {
            for(int i = position; i < definitionGroups.size(); i++) {
                definitionGroups.get(i).setOrder(i);
            }
        }

        return groupToRemove;
    }

    public void addDefinition(String type, String definition, String example) {
        int index = getGroupIndex(type);
        if(index != -1) {
            DefinitionGroupModel group = definitionGroups.get(index);
            if (group != null) {
                group.addNewDefinition(definition, example);
            }
        } else {
            DefinitionGroupModel newGroup = new DefinitionGroupModel(type, definitionGroups.size());
            newGroup.addNewDefinition(definition, example);
            definitionGroups.add(newGroup);
        }
    }

    public List<DefinitionGroupModel> getDefinitionGroups() {
        return definitionGroups;
    }

    public DefinitionGroupModel getFirstGroup() {
        return definitionGroups.isEmpty() ? null : definitionGroups.get(0);
    }

    public DefinitionGroupModel getLastGroup() {
        return definitionGroups.get(definitionGroups.size()-1);
    }

    public DefinitionGroupModel getDefinitionGroup(int index) {
        return definitionGroups.get(index);
    }

    public DefinitionGroupModel getDefinitionGroup(String type) {
        for (DefinitionGroupModel group : definitionGroups) {
            if (group.getType().equals(type)) return group;
        }

        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public boolean hasGroup(String type) {
        return getGroupIndex(type) != -1;
    }

    private int getGroupIndex(String type) {
        for(int i = 0; i < definitionGroups.size(); i++) {
            if (definitionGroups.get(i).getType().equals(type)) return i;
        }

        return -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(entry);
        parcel.writeString(pronunciation);
        parcel.writeTypedList(definitionGroups);
    }
}
