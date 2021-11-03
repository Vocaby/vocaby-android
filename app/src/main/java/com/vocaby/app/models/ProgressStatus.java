package com.vocaby.app.models;

public class ProgressStatus {
    private int max;
    private int current;

    public ProgressStatus(int max, int current) {
        this.max = max;
        this.current = current;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }
}
