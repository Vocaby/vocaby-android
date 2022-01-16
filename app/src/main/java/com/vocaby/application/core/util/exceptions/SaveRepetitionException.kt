package com.vocaby.application.core.util.exceptions

class SaveRepetitionException : Exception {
    constructor() : super()
    constructor(errorMessage: String?) : super(errorMessage)
}