package com.vocaby.application.core.util.exceptions

class IllegalFileException : Exception {
    val code: Int

    constructor(message: String?, code: Int) : super(message) {
        this.code = code
    }

    companion object {
        const val INVALID_FORMAT = 0
        const val INVALID_FILE = 1
    }
}