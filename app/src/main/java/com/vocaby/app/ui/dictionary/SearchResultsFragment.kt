package com.vocaby.app.ui.dictionary

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2.GONE
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.models.dictionary.EntryModel
import com.vocaby.app.models.viewstate.SaveStateModel
import com.vocaby.app.utils.LiveDataUtil.observeOnce
import com.vocaby.app.viewmodels.*
import kotlinx.android.synthetic.main.fragment_search_results.*

class SearchResultsFragment : Fragment() {
    private lateinit var ctx: Context
    private lateinit var searchedWord: String
    private val userViewModel: UserViewModelKt by activityViewModels()
    private val searchResultsViewModel: SearchResultsViewModel by viewModels{
        SearchResultsViewModelFactory(
            searchedWord,
            (requireActivity().application as VocabyApplication).repository,
            SaveStateModel(
                View.VISIBLE,
                R.drawable.ic_bookmark_disabled,
                R.drawable.ic_bookmark_unsaved,
                R.drawable.ic_bookmark_saved,
                R.string.save_button_unsaved,
                R.string.save_button_saved,
                R.color.gray,
                R.color.colorPrimary,
                false,
                false
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            searchedWord = requireArguments().getString(WORD).toString()
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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        search_results_body_pager.setPageTransformer(MarginPageTransformer(40))

        searchResultsViewModel.entryData.observeOnce(viewLifecycleOwner, { entryList ->
            entryList?.let {
                setupDictionary(entryList.size)
                search_results_body_pager.adapter = FragmentAdapter(this, entryList)
                search_progress.visibility = GONE
            }
        })

        searchResultsViewModel.missingDictionary.observeOnce(viewLifecycleOwner, { id ->
            val button = dictionary_selector.findViewById<RadioButton>(id)
            dictionary_selector.removeView(button)
            dictionary_selector.check(dictionary_selector.getChildAt(0).id)
        })

        // Observe changes to entry save state
        searchResultsViewModel.saveState.observe(viewLifecycleOwner, { saveState: SaveStateModel ->
            save_button.visibility = saveState.visibility
            save_button.text = getString(saveState.text)
            save_button.setTextColor(ctx.getColor(saveState.color))
            save_button.isEnabled = saveState.enabled
            val icon = AppCompatResources.getDrawable(ctx, saveState.icon)
            save_button.setTextColor(ctx.getColor(R.color.colorPrimaryAccent))
            save_button.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
            save_button.setOnClickListener {
                if (saveState.saved) {
                    userViewModel.removeSaveItem(searchedWord)
                } else {
                    userViewModel.addSaveItem(searchedWord)
                }
            }
        })
    }

    private fun setupDictionary(size: Int) {
        if (size > 1) {
            search_results_body_pager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 0) {
                        dictionary_selector.check(R.id.selection_custom)
                    } else {
                        dictionary_selector.check(R.id.selection_original)
                    }
                }
            })

            dictionary_selector.setOnCheckedChangeListener { _, id: Int ->
                if (id == R.id.selection_original) {
                    search_results_body_pager.currentItem = 1
                } else {
                    search_results_body_pager.currentItem = 0
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

    companion object {
        private const val WORD = "PASSED_WORD_KEY"
        @JvmStatic
        fun newInstance(passedWord: String?): SearchResultsFragment {
            val fragment = SearchResultsFragment()
            val args = Bundle()
            args.putString(WORD, passedWord)
            fragment.arguments = args
            return fragment
        }
    }
}