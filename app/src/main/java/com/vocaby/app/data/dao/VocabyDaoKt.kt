package com.vocaby.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vocaby.app.data.entity.Type
import com.vocaby.app.data.entity.User

@Dao
interface VocabyDaoKt {
    @Query("SELECT EXISTS(SELECT * FROM vocaby_user WHERE user_id = :id)")
    suspend fun checkUser(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createUser(user: User): Long

    @Insert
    fun insertTypes(vararg types: Type)
}