package com.vocaby.app.models.payload;

public interface PayloadState {
    int UNCHANGED = -1;
    int ADD = 0;
    int DELETE = 1;
    int UPDATE = 2;
}
