package com.vocaby.app

import android.app.Application
import com.vocaby.app.data.VocabyDatabase
import com.vocaby.app.data.VocabyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class VocabyApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { VocabyDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { VocabyRepository(database.vocabyDao(), this) }
}