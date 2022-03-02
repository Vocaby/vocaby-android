package com.vocaby.application.feature_dictionary.domain.use_case

import com.google.common.truth.Truth.assertThat
import com.vocaby.application.feature_dictionary.domain.model.SearchSuggestionItem
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetSearchSuggestionsUseCaseTest {
    private lateinit var getSearchSuggestionsUseCase: GetSearchSuggestionsUseCase

    @Before
    fun setup() {
        getSearchSuggestionsUseCase = GetSearchSuggestionsUseCase()
    }

    @Test
    fun `Search suggestions should have the correct items`() = runTest {
            val list = arrayListOf("a", "ab", "abc", "abra", "abraham", "d", "dagger", "dc", "king", "z")
            var searchSuggestions = getSearchSuggestionsUseCase.invoke("a", list, 4)
            assertThat(searchSuggestions).containsExactly(
                SearchSuggestionItem("a"),
                SearchSuggestionItem("ab"),
                SearchSuggestionItem("abc"),
                SearchSuggestionItem("abra"),
            ).inOrder()

            searchSuggestions = getSearchSuggestionsUseCase.invoke("ab", list, 4)
            assertThat(searchSuggestions).containsExactly(
                SearchSuggestionItem("ab"),
                SearchSuggestionItem("abc"),
                SearchSuggestionItem("abra"),
                SearchSuggestionItem("abraham")
            ).inOrder()

            searchSuggestions = getSearchSuggestionsUseCase.invoke("ab", list, 2)
            assertThat(searchSuggestions).containsExactly(
                SearchSuggestionItem("ab"),
                SearchSuggestionItem("abc")
            ).inOrder()

            searchSuggestions = getSearchSuggestionsUseCase.invoke("abr", list, 4)
            assertThat(searchSuggestions).containsExactly(
                SearchSuggestionItem("abra"),
                SearchSuggestionItem("abraham")
            ).inOrder()

            searchSuggestions = getSearchSuggestionsUseCase.invoke("abraham", list, 4)
            assertThat(searchSuggestions).containsExactly(
                SearchSuggestionItem("abraham")
            ).inOrder()

            searchSuggestions = getSearchSuggestionsUseCase.invoke("z", list, 4)
            assertThat(searchSuggestions).containsExactly(
                SearchSuggestionItem("z")
            ).inOrder()

            searchSuggestions = getSearchSuggestionsUseCase.invoke("b", list, 1)
            assertThat(searchSuggestions).isEmpty()

            searchSuggestions = getSearchSuggestionsUseCase.invoke("", list)
            assertThat(searchSuggestions).isEmpty()
    }
}