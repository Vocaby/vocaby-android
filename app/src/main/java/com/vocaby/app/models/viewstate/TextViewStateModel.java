package com.vocaby.app.models.viewstate;

public class TextViewStateModel extends ViewState {
    private final int text;

    public TextViewStateModel(int visibility, int text) {
        this.visibility = visibility;
        this.text = text;
    }

    public int getText() {
        return text;
    }
}
