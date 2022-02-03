package com.vocaby.application.core.data

import androidx.datastore.core.DataStore
import com.vocaby.app.ApplicationData
import com.vocaby.application.core.domain.repository.ApplicationRepository
import kotlinx.coroutines.flow.first

class ApplicationRepositoryImpl(
    private val applicationDataStore: DataStore<ApplicationData>
): ApplicationRepository {
    override suspend fun getLastTimeStarted(): Int {
        return applicationDataStore.data.first().lastTimeStarted
    }

    override suspend fun setLastStarted(dayOfYear: Int) {
        applicationDataStore.updateData { data ->
            data.toBuilder()
                .setLastTimeStarted(dayOfYear)
                .build()
        }
    }
}