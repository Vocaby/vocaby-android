package com.vocaby.app.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.vocaby.app.Constants
import com.vocaby.app.data.dao.VocabyDaoKt
import com.vocaby.app.data.entity.Type
import com.vocaby.app.data.entity.User
import com.vocaby.app.utils.Logger

class VocabyRepositoryKt(val dao: VocabyDaoKt, val application: Application) {
    private val vocabyDao: VocabyDaoKt = dao
    private val userSharedPreference: SharedPreferences =
        application.getSharedPreferences(Constants.USER_ID_KEY, Context.MODE_PRIVATE)
    private var userId: Int = userSharedPreference.getInt(Constants.CURRENT_USER_ID_KEY, 1)

    suspend fun setupUser() {
        val result = vocabyDao.checkUser(userId)
        if (result == 0) {
            createUser()
            Logger.reportToDebug("Created User using Kotlin")
        } else {
            Logger.reportToDebug("Retrieved User using Kotlin")
        }
    }

    private suspend fun createUser() {
        val longId = vocabyDao.createUser(User())
        userSharedPreference.edit().putInt(Constants.CURRENT_USER_ID_KEY, longId.toInt()).apply()
    }

    suspend fun insertType(vararg type: Type) {
        vocabyDao.insertTypes(*type)
    }
}