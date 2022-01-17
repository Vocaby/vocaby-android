package com.vocaby.application.feature_dictionary.presentation.ui

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.GONE
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.vocaby.application.R
import com.vocaby.application.core.util.LiveDataUtil.observeOnce
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.presentation.viewmodel.DictionaryViewModel
import com.vocaby.application.feature_dictionary.presentation.viewmodel.SearchResultsViewModel
import com.vocaby.application.feature_save.presentation.viewmodel.SaveViewModel
import com.vocaby.application.states.SaveState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchResultsFragment : Fragment() {
    private lateinit var ctx: Context
    private lateinit var searchedEntry: String
    private lateinit var viewPager: ViewPager2
    private lateinit var dictionarySelector: RadioGroup
    private lateinit var saveButton: Button
    private lateinit var searchProgress: ProgressBar
    private val saveViewModel: SaveViewModel by activityViewModels()
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

        if (savedInstanceState != null) {
            searchResultsViewModel.resetSaveState()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchResultsViewModel.entryData.observeOnce(viewLifecycleOwner) { entryList ->
            setupDictionary(entryList.size)
            dictionaryViewModel.writeToHistory(searchedEntry, entryList)
            viewPager.adapter = FragmentAdapter(this, entryList)
            dictionarySelector.visibility = View.VISIBLE
            searchProgress.visibility = GONE
        }

        searchResultsViewModel.missingDictionary.observeOnce(viewLifecycleOwner) { id ->
            val button = dictionarySelector.findViewById<RadioButton>(id)
            dictionarySelector.removeView(button)
            dictionarySelector.check(dictionarySelector.getChildAt(0).id)
        }

        // Observe changes to entry save state
        searchResultsViewModel.saveState.observe(viewLifecycleOwner) { saveState ->
            when (saveState) {
                is SaveState.Fetched -> {
                    saveButton.isEnabled = true
                    val icon: Drawable? = if (saveState.saved) {
                        AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_saved)
                    } else {
                        AppCompatResources.getDrawable(ctx, R.drawable.ic_bookmark_unsaved)
                    }

                    saveButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null)
                    saveButton.setOnClickListener {
                        if (saveState.saved) {
                            saveViewModel.removeSaveItem(searchedEntry)
                        } else {
                            saveViewModel.addSaveItem(searchedEntry)
                        }
                    }
                }

                is SaveState.InProgress -> {
                    saveButton.isEnabled = false
                }

                is SaveState.Remove -> {
                    saveButton.visibility = View.GONE
                }
            }
        }
    }

    private fun setupDictionary(size: Int) {
        if (size > 1) {
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
    private class FragmentAdapter(fragment: Fragment, var entryData: List<EntryModel?>) :
        FragmentStateAdapter(fragment) {
        override fun createFragment(position: Int): Fragment {
            if (entryData.size == 2) {
                if (position == 1) {
                    return SearchResultsBodyFragment.newInstance(entryData[1])
                }
            }
            return SearchResultsBodyFragment.newInstance(entryData[0])
        }

        override fun getItemCount(): Int {
            return entryData.size
        }
    }
}