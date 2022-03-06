package com.vocaby.application.feature_dictionary.data.local

import androidx.test.filters.SmallTest
import com.google.common.truth.Truth
import com.vocaby.application.core.data.VocabyDatabase
import com.vocaby.application.feature_dictionary.data.local.entity.Definition
import com.vocaby.application.feature_dictionary.data.local.entity.Entry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import javax.inject.Inject

@HiltAndroidTest
@SmallTest
@OptIn(ExperimentalCoroutinesApi::class)
class DictionaryDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: VocabyDatabase
    private lateinit var dao: DictionaryDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = database.dictionaryDao
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertEntries() = runTest {
        val words = listOf(
            Entry("enthusiasm", "", "", Date()),
            Entry("creativity", "", "", Date()),
        )

        dao.insertEntries(words)

        var exists = dao.checkEntryExistence("sage")
        Truth.assertThat(exists).isFalse()

        exists = dao.checkEntryExistence("enthusiasm")
        Truth.assertThat(exists).isTrue()
    }

    @Test
    fun insertDefinitions() = runTest {
        val words = listOf(
            Entry("enthusiasm", "", "", Date()),
            Entry("creativity", "", "", Date()),
            Entry("sacred", "", "", Date()),
            Entry("galvanize", "", "", Date()),
            Entry("fastidious", "", "", Date()),
            Entry("sage", "", "", Date()),
        )

        val definitions = listOf(
            Definition(1, "intense and eager enjoyment, interest, or approval.", "\"her energy and enthusiasm for life\"", "noun"),
            Definition(2, "the use of the imagination or original ideas, especially in the production of an artistic work.", "\"firms are keen to encourage creativity\"", "noun"),
            Definition(3, "connected with God (or the gods) or dedicated to a religious purpose and so deserving veneration.", "", "noun"),
            Definition(4, "shock or excite (someone) into taking action.", "\"the urgency of his voice galvanized them into action\"", "verb"),
            Definition(4, "coat (iron or steel) with a protective layer of zinc.", "\"they promised they would galvanize the iron railings to prevent rusting\"", "verb"),
            Definition(5, "very attentive to and concerned about accuracy and detail.", "\"he chooses his words with fastidious care\"", "adjective"),
            Definition(6, "a profoundly wise man, especially one who features in ancient history or legend.", "\"the sayings of the numerous venerable sages\"", "adjective"),
        )

        dao.insertEntries(words)
        dao.insertDefinitions(definitions)

        var entry = dao.getEntryDataWithId(1)
        Truth.assertThat(entry).isNotNull()
        Truth.assertThat(entry!!.definitions).hasSize(1)
        Truth.assertThat(entry.definitions.first().definition).isEqualTo("intense and eager enjoyment, interest, or approval.")

        entry = dao.getEntryData("galvanize")
        Truth.assertThat(entry).isNotNull()
        Truth.assertThat(entry!!.definitions).hasSize(2)
        Truth.assertThat(entry.definitions.first().definition).isEqualTo("shock or excite (someone) into taking action.")
        Truth.assertThat(entry.definitions.last().example).isEqualTo("\"they promised they would galvanize the iron railings to prevent rusting\"")
    }

    @Test
    fun getEntriesByCharacter() = runTest {
        val words = listOf(
            Entry("enthusiasm", "", "", Date()),
            Entry("creativity", "", "", Date()),
            Entry("sage", "", "", Date()),
            Entry("sacred", "", "", Date()),
            Entry("galvanize", "", "", Date()),
            Entry("fastidious", "", "", Date()),
            Entry("saga", "", "", Date()),
        )

        dao.insertEntries(words)

        var entries = dao.getDictionaryEntriesByCharacter("s")
        Truth.assertThat(entries).containsExactly("sacred", "saga", "sage")

        entries = dao.getDictionaryEntriesByCharacter("e")
        Truth.assertThat(entries).containsExactly("enthusiasm")

        entries = dao.getDictionaryEntriesByCharacter("z")
        Truth.assertThat(entries).isEmpty()
    }

    @Test
    fun getRandomWord() = runTest {
        val words = listOf("enthusiasm", "creativity", "sacred", "galvanize", "fastidious", "sage")
        val wordstoInsert = words.map { Entry(it, "", "", Date()) }

        val definitions = listOf(
            Definition(1, "intense and eager enjoyment, interest, or approval.", "\"her energy and enthusiasm for life\"", "noun"),
            Definition(2, "the use of the imagination or original ideas, especially in the production of an artistic work.", "\"firms are keen to encourage creativity\"", "noun"),
            Definition(3, "connected with God (or the gods) or dedicated to a religious purpose and so deserving veneration.", "", "noun"),
            Definition(4, "shock or excite (someone) into taking action.", "\"the urgency of his voice galvanized them into action\"", "verb"),
            Definition(4, "coat (iron or steel) with a protective layer of zinc.", "\"they promised they would galvanize the iron railings to prevent rusting\"", "verb"),
            Definition(5, "very attentive to and concerned about accuracy and detail.", "\"he chooses his words with fastidious care\"", "adjective"),
            Definition(6, "a profoundly wise man, especially one who features in ancient history or legend.", "\"the sayings of the numerous venerable sages\"", "adjective"),
        )

        dao.insertEntries(wordstoInsert)
        dao.insertDefinitions(definitions)

        var randomEntry = dao.getRandomWord()
        Truth.assertThat(randomEntry.entryData.entry).isIn(words)

        randomEntry = dao.getRandomWord()
        Truth.assertThat(randomEntry.entryData.entry).isIn(words)

        randomEntry = dao.getRandomWord()
        Truth.assertThat(randomEntry.entryData.entry).isIn(words)
    }
}