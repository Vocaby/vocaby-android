package com.vocaby.application.feature_profile.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_profile.data.local.entity.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
@OptIn(ExperimentalCoroutinesApi::class)
class UserDaoTest {
    private lateinit var database: VocabyDatabase
    private lateinit var dao: UserDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            VocabyDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.userDao
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun createUser() = runTest {
        val newUser = User()
        dao.createUser(newUser)
        val id = dao.getCurrentUser()
        assertThat(id).isEqualTo(1)
    }

    @Test
    fun checkUserExists() = runTest {
        var exists = dao.checkAnyUserExists()
        assertThat(exists).isFalse()

        val newUser = User()
        dao.createUser(newUser)

        exists = dao.checkAnyUserExists()
        assertThat(exists).isTrue()
    }
}