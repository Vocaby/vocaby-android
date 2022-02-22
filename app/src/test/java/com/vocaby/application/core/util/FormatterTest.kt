package com.vocaby.application.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.text.ParseException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class FormatterTest {
    @Test
    fun addDatePrefix() {
        val name = "test_name.txt"
        val localdate = LocalDate.of( 2022 , 1 , 1)
        val formatted = Formatter.addDatePrefix(name, Date.from(localdate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
        assertThat(formatted).isEqualTo("01012022_test_name.txt")
    }

    @Test
    fun formatDateToString() {
        val localtime = LocalTime.of(1, 13, 21)
        val localDate = LocalDate.of( 2022 , 1 , 1).atTime(localtime)
        val date = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant())
        var formatted = Formatter.formatDateToString(date.time, forDisplay = true, showDay = true)
        assertThat(formatted).isEqualTo("Sat 01.01.2022")

        formatted = Formatter.formatDateToString(date.time, forDisplay = true, showDay = false)
        assertThat(formatted).isEqualTo("01.01.2022")

        formatted = Formatter.formatDateToString(date.time, forDisplay = false, precise = true)
        assertThat(formatted).isEqualTo("2022-01-01 01:13:21")

        formatted = Formatter.formatDateToString(date.time, forDisplay = false, precise = false)
        assertThat(formatted).isEqualTo("2022-01-01")
    }

    @Test
    fun formatStringToDate() {
        val localtime = LocalTime.of(1, 13, 21)
        var localDate = LocalDate.of( 2022 , 1 , 1).atTime(localtime)
        var date = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant())
        var formatted = Formatter.formatStringToDate("2022-01-01 01:13:21")
        assertThat(formatted).isEqualTo(date)

        localDate = LocalDate.of( 2022 , 1 , 1).atStartOfDay()
        date = Date.from(localDate.atZone(ZoneId.systemDefault()).toInstant())
        formatted = Formatter.formatStringToDate("2022-01-01")
        assertThat(formatted).isEqualTo(date)
    }

    @Test(expected = ParseException::class)
    fun formatStringToDateException() {
        val dateString = "2022/01/03"
        Formatter.formatStringToDate(dateString)
    }

    @Test
    fun cleanText() {
        val formatted = Formatter.cleanText("abc{asa}\\whoa")
        assertThat(formatted).isEqualTo("abcasawhoa")
    }

    @Test
    fun firstLetterUpperCase() {
        val formatted = Formatter.firstLetterUpperCase("abc")
        assertThat(formatted).isEqualTo("Abc")
    }

    @Test
    fun cleanNumber() {
        var formatted = Formatter.cleanNumber(1000)
        assertThat(formatted).isEqualTo("1,000")

        formatted = Formatter.cleanNumber(1000, "Save", "Saves")
        assertThat(formatted).isEqualTo("1,000 Saves")

        formatted = Formatter.cleanNumber(1, "Save", "Saves")
        assertThat(formatted).isEqualTo("1 Save")
    }
}