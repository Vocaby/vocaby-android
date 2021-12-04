package com.vocaby.app.models.viewstate

class SaveStateModel(
    visibility: Int,
    private val disabledIcon: Int,
    private val unSavedIcon: Int,
    private val savedIcon: Int,
    private val unSavedText: Int,
    private val savedText: Int
) : ViewState(visibility) {
    var saved: Boolean = false
    var enabled: Boolean = false

    val text: Int
        get() {
            if (!enabled) {
                return unSavedText
            }
            return if (saved) savedText else unSavedText
        }

    val icon: Int
        get() {
            if (!enabled) {
                return disabledIcon
            }
            return if (saved) savedIcon else unSavedIcon
        }
}