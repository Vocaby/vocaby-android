package com.vocaby.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vocaby.app.data.dao.VocabyDaoKt
import com.vocaby.app.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [User::class, UserSave::class,
    Type::class, Word::class, Definition::class, CustomEntry::class, CustomDefinition::class,
    CustomEntryGroup::class], version = 1, exportSchema = false)
abstract class VocabyDatabaseKt : RoomDatabase() {
    abstract fun vocabyDao() : VocabyDaoKt

    companion object {
        @Volatile
        private var INSTANCE: VocabyDatabaseKt? = null

        fun getDatabase(context: Context, scope: CoroutineScope): VocabyDatabaseKt {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VocabyDatabaseKt::class.java,
                    "database"
                ).createFromAsset("databases/database.db").addCallback(Callback(scope)).build()

                INSTANCE = instance
                instance
            }
        }
    }

    private class Callback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.vocabyDao())
                }
            }
        }

        fun populateDatabase(dao: VocabyDaoKt) {
            val types = listOf(
                Type("noun"),
                Type("verb"),
                Type("adjective"),
                Type("adverb"),
                Type("idiom"),
                Type("proverb"),
                Type("phrase"),
                Type("preposition"),
                Type("interjection"),
                Type("conjunction"),
                Type("pronoun"),
            ).toTypedArray()

            dao.insertTypes(*types)
        }
    }
}