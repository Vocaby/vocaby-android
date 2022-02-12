package com.vocaby.application.core.util

data class UiText(var text: String? = null, val textResource: Int? = null, val prefix: String = "") {
    init {
        if (prefix.isNotEmpty())
            text = "$prefix: $text"
    }
}