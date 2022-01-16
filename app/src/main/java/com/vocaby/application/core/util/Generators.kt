package com.vocaby.application.core.util

import java.util.*

object Generators {
    fun generateRandomInt(min: Int, max: Int): Int {
        return Random().nextInt(max - min + 1) + min
    }
}