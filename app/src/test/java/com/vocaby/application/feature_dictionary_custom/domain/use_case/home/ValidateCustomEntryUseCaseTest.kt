package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

import com.vocaby.application.feature_dictionary_custom.domain.model.UserEntry
import com.vocaby.application.core.states.UserInputState
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class ValidateCustomEntryUseCaseTest {
    private lateinit var validateCustomEntry: ValidateCustomEntryUseCase
    private lateinit var listToTest: MutableList<UserEntry>

    @Before
    fun setup() {
        validateCustomEntry = ValidateCustomEntryUseCase()

        listToTest = mutableListOf()
        ('a'..'z').forEach { c ->
            listToTest.add(
                UserEntry(
                    c.toString(),
                    Date()
                )
            )
        }
    }

    @Test
    fun `Validator returns empty input state when given empty entry`() {
        val state = validateCustomEntry.invoke("", listToTest)
        assertTrue(state is UserInputState.EmptyInput)
    }

    @Test
    fun `Validator returns invalid state when entry has special characters`() {
        val state = validateCustomEntry.invoke("\\", listToTest)
        assertTrue(state is UserInputState.InvalidInput)
    }

    @Test
    fun `Validator returns long input state when entry is too long`() {
        val state = validateCustomEntry.invoke("A".repeat(101), listToTest)
        assertTrue(state is UserInputState.LongInput)
    }

    @Test
    fun `Validator returns same input state and correct index when entry exists`() {
        var state = validateCustomEntry.invoke("g", listToTest)
        assertTrue(state is UserInputState.SameInput<*>)
        state = state as UserInputState.SameInput<*>
        assertTrue(state.data is Int)
        assertTrue(state.data == 6)
    }

    @Test
    fun `Validator returns valid state and correct entry when given legal entry`() {
        var state = validateCustomEntry.invoke("ab", listToTest)
        assertTrue(state is UserInputState.Valid<*>)
        state = state as UserInputState.Valid<*>
        assertTrue(state.data is String)
        assertTrue(state.data == "ab")
    }
}