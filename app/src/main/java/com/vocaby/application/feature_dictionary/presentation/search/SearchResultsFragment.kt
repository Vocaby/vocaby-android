package com.vocaby.application.feature_dictionary.presentation.search

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.GONE
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    private lateinit var saveToCollectionDialog: BottomSheetDialog
    private lateinit var collectionAlert: TextView
    private lateinit var saveCollectionButton: Button
    private lateinit var removeSaveButton: Button
    private lateinit var collectionAdapter: CollectionAdapter
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

        setupCollectionCreateDialog()

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
                collectCollections()
            }

            launch {
                collectSaveState()
            }
        }

        return view
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
                            searchResultsViewModel.unsaveEntry()
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

    private suspend fun collectCollections() {
        searchResultsViewModel.saveCollections.collectLatest { collections ->
            collectionAdapter.setList(collections)
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

    private suspend fun collectUiEvent() {
        searchResultsViewModel.uiEvent.collect { event ->
            when(event) {
                is SearchUiEvent.ShowSnackBar -> {
                    val snackbar = Snackbar.make(
                        contextView,
                        event.message,
                        Snackbar.LENGTH_LONG
                    )

                    if (event.showAction) snackbar.setAction(R.string.snackbar_collection_action) {
                        searchResultsViewModel.saveToCollections()
                    }

                    snackbar.config(ctx, R.drawable.snackbar_background)
                    snackbar.show()
                }
                is SearchUiEvent.CloseCollectionDialog -> {
                    if (saveToCollectionDialog.isShowing) saveToCollectionDialog.dismiss()
                }
                is SearchUiEvent.ShowAddCollectionDialog -> {
                    saveCollectionButton.setOnClickListener {
                        saveCollectionButton.isEnabled = false
                        searchResultsViewModel.addEntryToCollections()
                    }
                    removeSaveButton.visibility = View.GONE
                    saveCollectionButton.setText(R.string.collection_save)
                    saveToCollectionDialog.show()
                }
                is SearchUiEvent.ShowUpdateCollectionDialog -> {
                    saveCollectionButton.setOnClickListener {
                        saveCollectionButton.isEnabled = false
                        searchResultsViewModel.updateItemInCollections()
                    }
                    removeSaveButton.visibility = View.VISIBLE
                    saveCollectionButton.setText(R.string.collection_update)
                    saveToCollectionDialog.show()
                }
                else -> {}
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

    private fun setupCollectionCreateDialog() {
        saveToCollectionDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        saveToCollectionDialog.setContentView(R.layout.dialog_save_collection)
        collectionAlert = saveToCollectionDialog.findViewById(R.id.header_alert)!!
        saveCollectionButton = saveToCollectionDialog.findViewById(R.id.save_button)!!
        val alertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())
        removeSaveButton = saveToCollectionDialog.findViewById(R.id.remove_button)!!
        removeSaveButton.setOnClickListener {
            saveToCollectionDialog.dismiss()
            alertDialogBuilder
                .setTitle("Remove from saved and collections?")
                .setMessage("Removing this save will also remove it from all collections")
                .setPositiveButton("REMOVE") { _, _ ->
                    searchResultsViewModel.unsaveEntry(true)
                }.setNegativeButton("CANCEL", null).create().show()
        }

        val builderRecyclerView = saveToCollectionDialog.findViewById<RecyclerView>(R.id.collection_container)!!
        builderRecyclerView.setHasFixedSize(true)
        builderRecyclerView.layoutManager =
            LinearLayoutManager(requireContext().applicationContext, LinearLayoutManager.HORIZONTAL, false)
        collectionAdapter = CollectionAdapter()
        builderRecyclerView.adapter = collectionAdapter

        // Clear content on show
        saveToCollectionDialog.setOnShowListener {
            collectionAlert.visibility = View.INVISIBLE
            saveCollectionButton.isEnabled = true
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