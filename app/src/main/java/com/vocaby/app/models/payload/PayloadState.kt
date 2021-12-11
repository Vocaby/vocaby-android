package com.vocaby.app.models.payload

interface PayloadState {
    companion object {
        const val UNCHANGED = -1
        const val ADD = 0
        const val DELETE = 1
        const val UPDATE = 2
    }
}