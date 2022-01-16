package com.vocaby.application.feature_customdictionary.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.vocaby.application.feature_dictionary.domain.model.DefinitionGroupModel;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class GroupChanges extends ItemsUpdate<DefinitionGroupModel> {
    int entryId;
    public GroupChanges(int entryId) {
        super(new HashMap<>(), new HashMap<>(), new LinkedHashMap<>());
        this.entryId = entryId;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(entryId);
    }

    protected GroupChanges(Parcel in) {
        super(in);
        entryId = in.readInt();
    }

    public static final Parcelable.Creator<GroupChanges> CREATOR =
            new Parcelable.Creator<GroupChanges>() {
        public GroupChanges createFromParcel(Parcel in) {
            return new GroupChanges(in);
        }

        public GroupChanges[] newArray(int size) {
            return new GroupChanges[size];
        }
    };

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public int getEntryId() {
        return entryId;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
