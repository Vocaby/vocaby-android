package com.vocaby.application.exceptions

class SaveRepetitionException : Exception {
    constructor() : super()
    constructor(errorMessage: String?) : super(errorMessage)
}