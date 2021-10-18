package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemStringPayload implements Parcelable, ItemPayload<String> {
    private int state;
    private final String payload;

    public ItemStringPayload(String payload) {
        this.state = UNCHANGED;
        this.payload = payload;
    }

    public ItemStringPayload(int state, String payload) {
        this.state = state;
        this.payload = payload;
    }

    protected ItemStringPayload(Parcel in) {
        state = in.readInt();
        payload = in.readString();
    }

    public static final Creator<ItemStringPayload> CREATOR = new Creator<ItemStringPayload>() {
        @Override
        public ItemStringPayload createFromParcel(Parcel in) {
            return new ItemStringPayload(in);
        }

        @Override
        public ItemStringPayload[] newArray(int size) {
            return new ItemStringPayload[size];
        }
    };

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void setState(int itemState) {
        state = itemState;
    }

    @Override
    public String getPayload() {
        return payload;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(state);
        parcel.writeString(payload);
    }
}
