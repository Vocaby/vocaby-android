package com.vocaby.application.feature_dictionary.presentation.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.vocaby.application.R
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.feature_dictionary.presentation.search.SearchResultsFragment
import com.vocaby.application.launchAndRepeatWithViewLifecycle
import com.vocaby.searchview.SearchView
import com.vocaby.searchview.suggestions.model.SearchSuggestion
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DictionaryFragment : Fragment() {
    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var searchView: SearchView

    private val dictionaryViewModel: DictionaryViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        if (childFragmentManager.backStackEntryCount > 0) backPressedCallback.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        backPressedCallback.isEnabled = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction().replace(
                R.id.dictionary_fragment_container,
                DictionaryHomeFragment()
            ).commit()
        }

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (childFragmentManager.backStackEntryCount > 0) {
                    childFragmentManager.popBackStack()
                    dictionaryViewModel.resetSearch()
                }
                if (childFragmentManager.backStackEntryCount == 0) {
                    this.isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, backPressedCallback)

        searchView = view.findViewById(R.id.vocaby_search)
        searchView.apply {
            setOnSearchListener(searchListener)
            setOnQueryChangeListener(queryChangeListener)
            setOnFocusChangeListener(searchFocusListener)
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                dictionaryViewModel.searchedEntry.collect { entry ->
                    addResultsFragment(entry)
                }
            }

            launch {
                dictionaryViewModel.searchSuggestions.collectLatest { result ->
                    when(result) {
                        is GenericState.InProgress -> searchView.showProgress()
                        is GenericState.Success -> {
                            searchView.hideProgress()
                            searchView.swapSuggestions(result.data)
                        }
                        is GenericState.Error -> {
                            searchView.clearSuggestions()
                            searchView.hideProgress()
                        }
                    }
                }
            }
        }

        return view
    }

    private fun addResultsFragment(search: String) {
        childFragmentManager.popBackStackImmediate()
        childFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
            ).add(
                R.id.dictionary_fragment_container,
                SearchResultsFragment.newInstance(search)
            ).addToBackStack(null).commit()

        backPressedCallback.isEnabled = true
    }

    fun search(entry: String) {
        dictionaryViewModel.search(entry)
    }

    private val searchListener: SearchView.OnSearchListener = object :
        SearchView.OnSearchListener {
        override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
            searchView.setOnQueryChangeListener(null)
            searchView.setSearchText(searchSuggestion.body)
            searchView.setOnQueryChangeListener(queryChangeListener)
            searchView.clearSearchFocus()
            search(searchSuggestion.body)
        }

        override fun onSearchAction(currentQuery: String) {
            search(currentQuery)
        }
    }

    private val searchFocusListener: SearchView.OnFocusChangeListener =
        object: SearchView.OnFocusChangeListener {
            override fun onFocus() {
                dictionaryViewModel.getSearchSuggestions(searchView.query)
            }

            override fun onFocusCleared() {
                searchView.clearSuggestions()
            }
        }

    private val queryChangeListener =
        object: SearchView.OnQueryChangeListener {
            override fun onSearchTextChanged(oldQuery: String, newQuery: String) {
                dictionaryViewModel.getSearchSuggestions(newQuery)
            }
        }
}