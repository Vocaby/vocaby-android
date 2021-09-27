package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class DefinitionChanges extends ItemsUpdate<DefinitionModel> {
    private int groupId;

    public DefinitionChanges() {
        super(new HashMap<>(), new HashMap<>(), new LinkedHashMap<>());
        groupId = -1;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(groupId);
    }

    protected DefinitionChanges(Parcel in) {
        super(in);
        groupId = in.readInt();
    }

    public DefinitionChanges(int groupId) {
        super(new HashMap<>(), new HashMap<>(), new LinkedHashMap<>());
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public static final Parcelable.Creator<DefinitionChanges> CREATOR =
            new Parcelable.Creator<DefinitionChanges>() {
        public DefinitionChanges createFromParcel(Parcel in) {
            return new DefinitionChanges(in);
        }

        public DefinitionChanges[] newArray(int size) {
            return new DefinitionChanges[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
