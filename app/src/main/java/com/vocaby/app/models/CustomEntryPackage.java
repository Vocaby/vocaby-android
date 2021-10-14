package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class CustomEntryPackage implements Parcelable {
    private String entry;
    private boolean isEdit;
    private boolean isDelete;

    public CustomEntryPackage(String entry, boolean isEdit) {
        this.entry = entry;
        this.isEdit = isEdit;
        isDelete = false;
    }

    protected CustomEntryPackage(Parcel in) {
        entry = in.readString();
        isEdit = in.readByte() != 0;
        isDelete = in.readByte() != 0;
    }

    public static final Creator<CustomEntryPackage> CREATOR = new Creator<CustomEntryPackage>() {
        @Override
        public CustomEntryPackage createFromParcel(Parcel in) {
            return new CustomEntryPackage(in);
        }

        @Override
        public CustomEntryPackage[] newArray(int size) {
            return new CustomEntryPackage[size];
        }
    };

    public String getEntry() {
        return entry;
    }

    public void setEntry(String entry) {
        this.entry = entry;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(entry);
        parcel.writeByte((byte) (isEdit ? 1 : 0));
        parcel.writeByte((byte) (isDelete ? 1 : 0));
    }
}
