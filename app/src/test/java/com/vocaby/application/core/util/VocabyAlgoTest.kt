package com.vocaby.application.core.util

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class VocabyAlgoTest {
    @Test
    fun `Test prefix binary search algorithm`() {
        runBlocking {
            val list = arrayListOf("a", "ab", "abc", "abra", "abraham", "d", "dagger", "dc", "king", "z")
            var index = VocabyAlgo.binarySearchPrefix(list, "abr")
            assertThat(index).isEqualTo(3)

            index = VocabyAlgo.binarySearchPrefix(list, "ab")
            assertThat(index).isEqualTo(1)

            index = VocabyAlgo.binarySearchPrefix(list, "a")
            assertThat(index).isEqualTo(0)

            index = VocabyAlgo.binarySearchPrefix(list, "d")
            assertThat(index).isEqualTo(5)

            index = VocabyAlgo.binarySearchPrefix(list, "dagger")
            assertThat(index).isEqualTo(6)

            index = VocabyAlgo.binarySearchPrefix(list, "z")
            assertThat(index).isEqualTo(9)

            index = VocabyAlgo.binarySearchPrefix(list, "zd")
            assertThat(index).isEqualTo(-1)

            index = VocabyAlgo.binarySearchPrefix(list, "")
            assertThat(index).isEqualTo(-1)
        }
    }
}