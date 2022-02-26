package com.vocaby.application.core.util.exceptions

class IllegalFileException(message: String?, val code: Int) : Exception(message) {

    companion object {
        const val INVALID_FORMAT = 0
        const val INVALID_FILE = 1
    }
}