package com.vocaby.app.models;

public interface ItemPayload<T> {
    int UNCHANGED = -1;
    int ADD = 0;
    int DELETE = 1;
    int UPDATE = 2;

    int getState();
    void setState(int itemState);
    T getPayload();
}
