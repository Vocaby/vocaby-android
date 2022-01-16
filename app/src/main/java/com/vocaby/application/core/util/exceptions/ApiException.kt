package com.vocaby.application.core.util.exceptions

class ApiException : Exception {
    constructor() : super()
    constructor(errorMessage: String?) : super(errorMessage)
}