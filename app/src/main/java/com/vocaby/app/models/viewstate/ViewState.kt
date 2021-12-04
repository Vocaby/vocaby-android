package com.vocaby.app.models.viewstate;

import android.view.View;

public abstract class ViewState {
    int visibility;

    public int getVisibility() {
        return visibility == View.VISIBLE ? View.VISIBLE : View.GONE;
    }
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }
}
