package com.vocaby.application.exceptions

class ApiException : Exception {
    constructor() : super()
    constructor(errorMessage: String?) : super(errorMessage)
}