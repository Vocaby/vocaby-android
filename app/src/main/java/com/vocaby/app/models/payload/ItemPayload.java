package com.vocaby.app.models.payload;

public interface ItemPayload<T> extends PayloadState {
    int getState();
    void setState(int itemState);
    T getPayload();
}
