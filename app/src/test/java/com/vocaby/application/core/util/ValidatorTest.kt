package com.vocaby.application.core.util

import com.google.common.truth.Truth.assertThat
import com.vocaby.application.core.Constants
import com.vocaby.application.feature_dictionary_custom.common.Constants.ENTRY_MAX_LENGTH
import org.junit.Test

class ValidatorTest {

    @Test
    fun dateIsValid() {
        var date = "2022-01-02"
        assertThat(Validator.dateIsValid(date)).isTrue()

        date = "2022-01-02 12:23:01"
        assertThat(Validator.dateIsValid(date)).isTrue()

        date = "Sun 2022-01-02 12:23:01"
        assertThat(Validator.dateIsValid(date)).isFalse()
    }

    @Test
    fun containsSpecialCharacter() {
        var string = "asdasd { asdasd a"
        assertThat(Validator.containsSpecialCharacter(string)).isTrue()

        string = "aA!@#*!(@$!*%)!@[]~`|\""
        assertThat(Validator.containsSpecialCharacter(string)).isFalse()
    }

    @Test
    fun validateEntry() {
        var string = "a".repeat(ENTRY_MAX_LENGTH)
        assertThat(Validator.entryIsValid(string)).isTrue()

        "{".repeat(ENTRY_MAX_LENGTH)
        assertThat(Validator.entryIsValid(string)).isTrue()

        string = "a".repeat(ENTRY_MAX_LENGTH + 1)
        assertThat(Validator.entryIsValid(string)).isFalse()

    }
}