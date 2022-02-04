package com.vocaby.application.feature_dictionary.presentation.search

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.GONE
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.vocaby.application.R
import com.vocaby.application.core.util.ResourceState
import com.vocaby.application.core.util.config
import com.vocaby.application.feature_dictionary.domain.model.DictionarySearchResult
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryViewModel
import com.vocaby.application.feature_save.presentation.save.SaveState
import com.vocaby.application.launchAndRepeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchResultsFragment : Fragment() {
    private lateinit var ctx: Context
    private lateinit var searchedEntry: String
    private lateinit var viewPager: ViewPager2
    private lateinit var dictionarySelector: RadioGroup
    private lateinit var saveButton: Button
    private lateinit var searchProgress: ProgressBar
    private lateinit var contextView: CoordinatorLayout

    private val searchResultsViewModel: SearchResultsViewModel by viewModels()
    private val dictionaryViewModel: DictionaryViewModel by activityViewModels()

    companion object {
        const val ENTRY = "PASSED_ENTRY_KEY"
        @JvmStatic
        fun newInstance(passedWord: String?): SearchResultsFragment {
            val fragment = SearchResultsFragment()
            val args = Bundle()
            args.putString(ENTRY, passedWord)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            searchedEntry = requireArguments().getString(ENTRY).toString()
        }

        ctx = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_results, container, false)
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener(backListener)

        val saveProgress = view.findViewById<ProgressBar>(R.id.save_progress)
        saveProgress.visibility = GONE

        viewPager = view.findViewById(R.id.search_results_body_pager)
        dictionarySelector = view.findViewById(R.id.dictionary_selector)
        saveButton = view.findViewById(R.id.save_button)
        searchProgress = view.findViewById(R.id.search_progress)
        contextView = view.findViewById(R.id.search_results_coordinator_layout)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            launch {
                collectUiEvent()
            }

            launch {
                collectSearchState()
            }

            launch {
                collectDictionarySelectorState()
            }

            launch {
                collectSaveState()
            }
        }
    }

    // TODO: private suspend fun collectUiState() {}

    private suspend fun collectSaveState() {
        searchResultsViewModel.saveState.collect { saveState ->
            when (saveState) {
                is SaveState.Processed -> {
                    saveButton.isEnabled = true
                    val icon: Drawable? = if (saveState.saved) {
                        AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved)
                    } else {
                        AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved)
                    }

                    saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null)
                    saveButton.setOnClickListener {
                        if (saveState.saved) {
                            searchResultsViewModel.removeSavedEntry()
                        } else {
                            searchResultsViewModel.saveEntry()
                        }
                    }
                }

                is SaveState.InProgress -> {
                    searchProgress.visibility = View.VISIBLE
                    saveButton.isEnabled = false
                }

                is SaveState.Remove -> {
                    saveButton.visibility = View.GONE
                }

                is SaveState.Show -> {
                    saveButton.visibility = View.VISIBLE
                }
            }
        }
    }

    private suspend fun collectDictionarySelectorState() {
        searchResultsViewModel.dictionarySelectorState.collectLatest { selectorState ->
            val buttonToHide = dictionarySelector.findViewById<RadioButton>(selectorState.hideId)
            val buttonToShow = dictionarySelector.findViewById<RadioButton>(selectorState.displayId)
            if (selectorState.displayAll) {
                buttonToHide.visibility = View.VISIBLE
                buttonToShow.visibility = View.VISIBLE
            } else {
                buttonToHide.visibility = View.GONE
                buttonToShow.visibility = View.VISIBLE
            }

            dictionarySelector.check(selectorState.displayId)
        }
    }

    private suspend fun collectSearchState() {
        searchResultsViewModel.entryData.collectLatest { searchState ->
            when (searchState) {
                is ResourceState.InProgress -> searchProgress.visibility = View.VISIBLE
                is ResourceState.Success -> {
                    searchState.data?.let { dictionaryResult ->
                        setupDictionary(dictionaryResult)
                        dictionaryViewModel.writeToHistory(searchedEntry, dictionaryResult)
                        viewPager.adapter = FragmentAdapter(this@SearchResultsFragment, dictionaryResult)
                        dictionarySelector.visibility = View.VISIBLE
                        searchProgress.visibility = GONE
                    }
                }
                else -> {}
            }
        }
    }

    private fun showSnackBar(message: String, showAction: Boolean = false) {
        val snackbar = Snackbar.make(
            contextView,
            message,
            Snackbar.LENGTH_LONG
        )

        if (showAction) snackbar.setAction(R.string.snackbar_collection_action) {
            searchResultsViewModel.saveToCollections()
        }

        snackbar.config(ctx, R.drawable.snackbar_background)
        snackbar.show()
    }

    private suspend fun collectUiEvent() {
        searchResultsViewModel.uiEvent.collect { event ->
            when(event) {
                is SearchUiEvent.ShowSnackBar -> {
                    showSnackBar(event.message, event.showAction)
                }
                is SearchUiEvent.ShowCollectionDialog -> {
                    val dialogFragment = SearchCollectionDialogFragment.newInstance(
                        event.updateMode,
                        event.saveModel,
                        event.collections
                    )

                    childFragmentManager.setFragmentResultListener(SearchCollectionDialogFragment.TAG, viewLifecycleOwner) { _, bundle ->
                        if (bundle.getBoolean(SearchCollectionDialogFragment.REMOVE_ALL)) {
                            val alertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())
                            alertDialogBuilder
                                .setTitle("Remove from saved and collections?")
                                .setMessage("Removing this save will also remove it from all collections")
                                .setPositiveButton("REMOVE") { _, _ ->
                                    searchResultsViewModel.removeSavedEntry(true)
                                }.setNegativeButton("CANCEL", null).create().show()
                        } else {
                            val showSnackbar = bundle.getBoolean(SearchCollectionDialogFragment.SHOW_SNACKBAR)
                            if (showSnackbar) showSnackBar("Collections have been updated")
                        }
                    }

                    dialogFragment.show(childFragmentManager, SearchCollectionDialogFragment.TAG)
                }
            }
        }
    }

    private fun setupDictionary(dictionarySearchResult: DictionarySearchResult) {
        if (dictionarySearchResult.size > 1) {
            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 0) {
                        dictionarySelector.check(R.id.selection_custom)
                    } else {
                        dictionarySelector.check(R.id.selection_original)
                    }
                }
            })

            dictionarySelector.setOnCheckedChangeListener { _, id: Int ->
                if (id == R.id.selection_original) {
                    viewPager.currentItem = 1
                } else {
                    viewPager.currentItem = 0
                }
            }
        }
    }

    private val backListener = View.OnClickListener { requireActivity().onBackPressed() }

    // ViewPager Adapter for Different Dictionary Definitions
    private class FragmentAdapter(
        fragment: Fragment,
        var entryData: DictionarySearchResult
    ): FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            if (entryData.size == 2) {
                if (position == 1) {
                    return SearchResultsBodyFragment.newInstance(entryData.originalModel)
                }
            }

            return SearchResultsBodyFragment.newInstance(entryData.data)
        }

        override fun getItemCount(): Int {
            return entryData.size
        }
    }
}