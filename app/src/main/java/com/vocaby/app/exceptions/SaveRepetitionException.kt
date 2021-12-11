package com.vocaby.app.exceptions

class SaveRepetitionException : Exception {
    constructor() : super()
    constructor(errorMessage: String?) : super(errorMessage)
}