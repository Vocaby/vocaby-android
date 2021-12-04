package com.vocaby.app.models.viewstate;

public class SaveStateModel extends ViewState {
    private final int disabledIcon;
    private final int unSavedIcon;
    private final int savedIcon;
    private final int unSavedText;
    private final int savedText;
    private final int disabledTextColor;
    private final int enabledTextColor;
    private boolean saved;
    private boolean enabled;

    public SaveStateModel(int visibility, int disabledIcon, int unSavedIcon, int savedIcon,
                          int unSavedText, int savedText, int disabledTextColor,
                          int enabledTextColor, boolean saved, boolean enabled) {
        this.visibility = visibility;
        this.disabledIcon = disabledIcon;
        this.unSavedIcon = unSavedIcon;
        this.savedIcon = savedIcon;
        this.unSavedText = unSavedText;
        this.savedText = savedText;
        this.disabledTextColor = disabledTextColor;
        this.enabledTextColor = enabledTextColor;
        this.saved = saved;
        this.enabled = enabled;
    }

    public int getText() {
        if (!enabled) {
            return unSavedText;
        }

        return saved  ? savedText : unSavedText;
    }

    public int getIcon() {
        if (!enabled) {
            return disabledIcon;
        }

        return saved ? savedIcon : unSavedIcon;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean getSaved() {
        return this.saved;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public int getColor() {
        return enabled ? disabledTextColor : enabledTextColor;
    }
}
