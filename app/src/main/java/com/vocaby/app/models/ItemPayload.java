package com.vocaby.app.models;

public class ItemPayload<T> {
    public static final int UNCHANGED = -1;
    public static final int ADD = 0;
    public static final int DELETE = 1;
    public static final int UPDATE = 2;

    private int state;
    private final T payload;

    public ItemPayload(T payload) {
        this.state = UNCHANGED;
        this.payload = payload;
    }

    public ItemPayload(int state, T payload) {
        this.state = state;
        this.payload = payload;
    }

    public int getState() {
        return state;
    }

    public void setState(int itemState) {
        state = itemState;
    }

    public T getPayload() {
        return payload;
    }
}
