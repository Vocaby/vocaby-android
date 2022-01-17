package com.vocaby.application.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vocaby.application.feature_dictionary_custom.data.local.CustomDictionaryDao
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomDefinition
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntry
import com.vocaby.application.feature_dictionary_custom.data.local.entity.CustomEntryGroup
import com.vocaby.application.feature_dictionary.data.local.DictionaryDao
import com.vocaby.application.feature_dictionary.data.local.entity.Definition
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary.data.local.entity.Word
import com.vocaby.application.feature_user.data.local.UserDao
import com.vocaby.application.feature_user.data.local.entity.CustomDictionaryViewCount
import com.vocaby.application.feature_user.data.local.entity.DictionaryViewCount
import com.vocaby.application.feature_user.data.local.entity.User
import com.vocaby.application.feature_user.data.local.entity.UserSave
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(entities = [User::class, UserSave::class,
    Type::class, Word::class, Definition::class, CustomEntry::class, CustomDefinition::class,
    CustomEntryGroup::class, DictionaryViewCount::class, CustomDictionaryViewCount::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class VocabyDatabase : RoomDatabase() {
    abstract val dictionaryDao: DictionaryDao
    abstract val customDictionaryDao: CustomDictionaryDao
    abstract val userDao: UserDao

    companion object {
        fun getDatabase(context: Context): VocabyDatabase {
            return Room.databaseBuilder(
                context,
                VocabyDatabase::class.java,
                "database"
            ).createFromAsset("databases/vocabydb.db")
            .setJournalMode(JournalMode.AUTOMATIC)
            .addCallback(object: Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    val applicationScope = CoroutineScope(SupervisorJob())
                    applicationScope.launch {
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

                        getDatabase(context).customDictionaryDao.insertTypes(*types)
                    }
                }
            })
            .build()

        }
    }
}