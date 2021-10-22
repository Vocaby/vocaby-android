package com.vocaby.app.models;

public interface ItemPayload<T> extends ItemState {
    int getState();
    void setState(int itemState);
    T getPayload();
}
