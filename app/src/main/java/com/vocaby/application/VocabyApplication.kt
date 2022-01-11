package com.vocaby.application

import android.app.Application
import com.vocaby.application.data.VocabyDatabase
import com.vocaby.application.data.VocabyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class VocabyApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { VocabyDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { VocabyRepository(database.vocabyDao(), this) }
}