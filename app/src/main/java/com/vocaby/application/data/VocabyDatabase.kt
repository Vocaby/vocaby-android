package com.vocaby.application.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vocaby.application.data.dao.VocabyDao
import com.vocaby.application.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, UserSave::class,
    Type::class, Word::class, Definition::class, CustomEntry::class, CustomDefinition::class,
    CustomEntryGroup::class, DictionaryViewCount::class, CustomDictionaryViewCount::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ]
)
abstract class VocabyDatabase : RoomDatabase() {
    abstract fun vocabyDao() : VocabyDao

    companion object {
        @Volatile
        private var INSTANCE: VocabyDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): VocabyDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VocabyDatabase::class.java,
                    "database"
                ).createFromAsset("databases/vocabydb.db")
                .setJournalMode(JournalMode.TRUNCATE)
                .addCallback(Callback(scope))
                .build()

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
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.vocabyDao())
                }
            }
        }

        fun populateDatabase(dao: VocabyDao) {
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