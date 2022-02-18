package com.vocaby.application.feature_profile.data.local

import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_profile.data.local.entity.User
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
@OptIn(ExperimentalCoroutinesApi::class)
class UserDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: VocabyDatabase
    private lateinit var dao: UserDao

    @Before
    fun setup() {
        hiltRule.inject()
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
        val newUser = User()
        dao.createUser(newUser)

        var exists = dao.checkUserExists(1)
        assertThat(exists).isTrue()

        exists = dao.checkUserExists(2)
        assertThat(exists).isFalse()
    }

    @Test
    fun checkAnyUserExists() = runTest {
        var exists = dao.checkAnyUserExists()
        assertThat(exists).isFalse()

        val newUser = User()
        dao.createUser(newUser)

        exists = dao.checkAnyUserExists()
        assertThat(exists).isTrue()
    }

    @Test
    fun deleteUser() = runTest {
        val newUser = User()
        val id = dao.createUser(newUser)
        dao.deleteUser(User(userId = id.toInt()))

        val exists = dao.checkUserExists(id.toInt())
        assertThat(exists).isFalse()
    }

    @Test
    fun cleanupUser() = runTest {
        val newUser = User()
        val id = dao.createUser(newUser)
        val id2 = dao.createUser(newUser)
        val id3 = dao.createUser(newUser)
        val id4 = dao.createUser(newUser)

        dao.cleanupUser(id.toInt())
        var exists = dao.checkUserExists(id.toInt())
        assertThat(exists).isTrue()

        exists = dao.checkUserExists(id2.toInt())
        assertThat(exists).isFalse()

        exists = dao.checkUserExists(id3.toInt())
        assertThat(exists).isFalse()

        exists = dao.checkUserExists(id4.toInt())
        assertThat(exists).isFalse()
    }
}