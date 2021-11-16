package com.vocaby.app

import android.app.Application
import com.vocaby.app.data.VocabyDatabaseKt
import com.vocaby.app.data.VocabyRepositoryKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class VocabyApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { VocabyDatabaseKt.getDatabase(this, applicationScope) }
    val repository by lazy { VocabyRepositoryKt(database.vocabyDao(), this) }
}