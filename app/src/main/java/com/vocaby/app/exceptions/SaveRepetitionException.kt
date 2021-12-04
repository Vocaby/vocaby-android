package com.vocaby.app.exceptions

import java.lang.Exception

class SaveRepetitionException : Exception {
    constructor() : super()
    constructor(errorMessage: String?) : super(errorMessage)
}