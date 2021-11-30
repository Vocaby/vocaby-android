package com.vocaby.app.ui.dictionary

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.FloatingSearchView.OnQueryChangeListener
import com.arlib.floatingsearchview.FloatingSearchView.OnSearchListener
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion
import com.vocaby.app.R
import com.vocaby.app.VocabyApplication
import com.vocaby.app.adapters.SearchHistoryAdapter
import com.vocaby.app.states.GenericState
import com.vocaby.app.ui.dictionary.SearchResultsFragment.Companion.newInstance
import com.vocaby.app.viewmodels.DictionaryViewModel
import com.vocaby.app.viewmodels.DictionaryViewModelFactory

class DictionaryHomeFragment : Fragment(), SearchHistoryAdapter.OnItemTouchListener {
    private lateinit var ctx: Context
    private lateinit var searchView: FloatingSearchView
    private lateinit var wordView: TextView
    private lateinit var posView: TextView
    private lateinit var definition: TextView
    private lateinit var sentence: TextView
    private lateinit var wordBox: View
    private lateinit var progressBar: ProgressBar
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter

    private val dictionaryViewModel: DictionaryViewModel by activityViewModels {
        DictionaryViewModelFactory((requireActivity().application as VocabyApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dictionary_main, container, false)

        // Random Word of the Day
        wordView = view.findViewById(R.id.word_header)
        posView = view.findViewById(R.id.pos)
        definition = view.findViewById(R.id.card_definition)
        sentence = view.findViewById(R.id.card_sentence)
        wordBox = view.findViewById(R.id.word_box)
        progressBar = view.findViewById(R.id.randomword_progress)

        definition.visibility = View.GONE
        sentence.visibility = View.GONE
        posView.visibility = View.GONE

        searchView = view.findViewById(R.id.vocaby_search_bar)
        searchView.setOnSearchListener(searchListener)
        searchView.setOnQueryChangeListener(queryChangeListener)
        searchView.setOnFocusChangeListener(searchFocusListener)


        setUpHistoryRecyclerView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dictionaryViewModel.searchedEntry.observe(viewLifecycleOwner) { entry ->
            addResultsFragment(entry)
        }

        dictionaryViewModel.searchHistory.observe(viewLifecycleOwner) { searchHistory ->
            searchHistoryAdapter.updateSearchHistory(
                searchHistory
            )
        }

        dictionaryViewModel.randomEntry.observe(viewLifecycleOwner, { randomEntryModel ->
            wordView.text = randomEntryModel.entry
            posView.text =  randomEntryModel.firstGroup.type
            definition.text = randomEntryModel.firstGroup.definitionData[0].toString()
            sentence.text = randomEntryModel.firstGroup.definitionData[0].example

            definition.visibility = View.VISIBLE
            sentence.visibility = View.VISIBLE
            posView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE

            wordBox.setOnClickListener { dictionaryViewModel.search(randomEntryModel.entry) }
        })

        dictionaryViewModel.searchSuggestions.observe(viewLifecycleOwner,
            { result ->
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
        )
    }

    private fun addResultsFragment(search: String) {
        val fm = parentFragmentManager
        fm.popBackStackImmediate()
        fm.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
            ).add(
                R.id.dictionary_fragment_container,
                newInstance(search)
            ).addToBackStack(null).commit()
    }

    private fun setUpHistoryRecyclerView(view: View) {
        val historyContainer: RecyclerView = view.findViewById(R.id.search_history_container)
        searchHistoryAdapter = SearchHistoryAdapter(ctx, this)
        historyContainer.adapter = searchHistoryAdapter
        historyContainer.layoutManager =
            LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
    }

    fun search(entry: String) {
        dictionaryViewModel.search(entry)
    }

    private val searchListener: OnSearchListener = object : OnSearchListener {
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

    private val searchFocusListener: FloatingSearchView.OnFocusChangeListener =
        object: FloatingSearchView.OnFocusChangeListener {
            override fun onFocus() {
                dictionaryViewModel.getSearchSuggestions("", searchView.query)
            }

            override fun onFocusCleared() {
                searchView.clearSuggestions()
            }
        }

    private val queryChangeListener =
        OnQueryChangeListener { oldQuery: String, newQuery: String ->
            dictionaryViewModel.getSearchSuggestions(
                oldQuery,
                newQuery
            )
        }

    override fun onResume() {
        super.onResume()
        dictionaryViewModel.updateRandomWord()
    }

    override fun onItemTouch(position: Int) {
        dictionaryViewModel.getHistoryDefinition(position)
    }
}