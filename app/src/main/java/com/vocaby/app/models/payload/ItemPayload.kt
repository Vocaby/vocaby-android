package com.vocaby.app.models.payload

interface ItemPayload<T> : PayloadState {
    var state: Int
    val payload: T
}