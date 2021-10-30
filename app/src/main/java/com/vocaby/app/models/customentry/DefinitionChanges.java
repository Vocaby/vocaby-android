package com.vocaby.app.models.customentry;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.vocaby.app.Constants;
import com.vocaby.app.models.ItemsUpdate;
import com.vocaby.app.models.dictionary.DefinitionModel;

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

    @Override
    public void printDebug() {
        for (DefinitionModel definitionModel : getAddedItems()) {
            Log.d(Constants.DEBUG_TAG, "Added Item " + definitionModel.hashCode() + ": " + definitionModel.getDefinition() + " > " + definitionModel.getOrder());
        }

        for (DefinitionModel definitionModel : getDeletedItems()) {
            Log.d(Constants.DEBUG_TAG, "Deleted Item " + definitionModel.hashCode() + ": " + definitionModel.getDefinition() + " > " + definitionModel.getOrder());
        }

        for (DefinitionModel definitionModel : getUpdatedItems()) {
            Log.d(Constants.DEBUG_TAG, "Updated Item "+ definitionModel.hashCode() + ": "  + definitionModel.getDefinition() + " > " + definitionModel.getOrder());
        }
    }
}
