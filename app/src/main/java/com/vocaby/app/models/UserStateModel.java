package com.vocaby.app.models;

public class UserStateModel {
    private boolean isLocal;
    private boolean isSynced;

    public UserStateModel(boolean isLocal) {
        this.isLocal = isLocal;
        this.isSynced = true;
    }

    public UserStateModel(boolean isLocal, boolean isSynced) {
        this.isLocal = isLocal;
        this.isSynced = isSynced;
    }

    public boolean isLocal() {
        return this.isLocal;
    }

    public boolean isSynced() {
        return this.isSynced;
    }
}
