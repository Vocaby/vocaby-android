package com.vocaby.app.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemIntPayload implements Parcelable, ItemPayload<Integer> {
    private int state;
    private final int payload;

    public ItemIntPayload(int payload) {
        this.state = UNCHANGED;
        this.payload = payload;
    }

    public ItemIntPayload(int state, int payload) {
        this.state = state;
        this.payload = payload;
    }

    protected ItemIntPayload(Parcel in) {
        state = in.readInt();
        payload = in.readInt();
    }

    public static final Creator<ItemIntPayload> CREATOR = new Creator<ItemIntPayload>() {
        @Override
        public ItemIntPayload createFromParcel(Parcel in) {
            return new ItemIntPayload(in);
        }

        @Override
        public ItemIntPayload[] newArray(int size) {
            return new ItemIntPayload[size];
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
    public Integer getPayload() {
        return payload;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(state);
        parcel.writeInt(payload);
    }
}
