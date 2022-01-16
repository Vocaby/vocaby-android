package com.vocaby.application.feature_dictionary.presentation.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.core.util.Formatter.formatDateToString
import com.vocaby.application.feature_dictionary.presentation.adapter.SearchHistoryAdapter
import com.vocaby.application.feature_dictionary.presentation.viewmodel.DictionaryViewModel
import java.util.*

class DictionaryHomeFragment : Fragment(), SearchHistoryAdapter.OnItemTouchListener {
    private lateinit var ctx: Context
    private lateinit var wordView: TextView
    private lateinit var posView: TextView
    private lateinit var definition: TextView
    private lateinit var sentence: TextView
    private lateinit var wordBox: View
    private lateinit var wordBoxTag: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var emptyCard: LinearLayout

    private val dictionaryViewModel: DictionaryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dictionary_main, container, false)

        val dateView = view.findViewById<TextView>(R.id.date)
        dateView.text = formatDateToString(Date().time, true, showDay = true)

        // Random Word of the Day
        wordView = view.findViewById(R.id.entry_header)
        posView = view.findViewById(R.id.pos)
        definition = view.findViewById(R.id.card_definition)
        sentence = view.findViewById(R.id.card_sentence)
        wordBox = view.findViewById(R.id.word_box)
        progressBar = view.findViewById(R.id.randomword_progress)
        wordBoxTag = view.findViewById(R.id.word_box_tag)

        progressBar.visibility = View.VISIBLE
        definition.visibility = View.GONE
        sentence.visibility = View.GONE
        posView.visibility = View.GONE

        emptyCard = view.findViewById(R.id.empty_card)

        setUpHistoryRecyclerView(view)
        dictionaryViewModel.getHistory()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dictionaryViewModel.searchHistory.observe(viewLifecycleOwner) { searchHistory ->
            searchHistory?.let {
                searchHistoryAdapter.updateSearchHistory(
                    searchHistory
                )

                if (searchHistory.isNotEmpty()) emptyCard.visibility = View.INVISIBLE
                else emptyCard.visibility = View.VISIBLE
            }
        }

        dictionaryViewModel.dailyPick.observe(viewLifecycleOwner, { dailyPick ->
            dailyPick.entryModel?.let { entryModel ->
                wordView.text = entryModel.entry
                posView.text =  entryModel.firstGroup.type
                definition.text = entryModel.firstGroup.definitionData[0].toString()

                entryModel.firstGroup.definitionData[0].example?.let { example ->
                    if (example.isNotEmpty()) {
                        sentence.visibility = View.VISIBLE
                        sentence.text = example
                    }
                }

                wordBoxTag.visibility = View.VISIBLE
                if (dailyPick.random) {
                    wordBoxTag.text = getString(R.string.wod_random_pick)
                    wordBoxTag.setTextColor(ContextCompat.getColor(ctx, R.color.colorHeadline))
                    wordBoxTag.background.setTint(ContextCompat.getColor(ctx, R.color.colorHeadlineSoft))
                } else {
                    wordBoxTag.text = getString(R.string.wod_our_pick)
                    wordBoxTag.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimaryAccent))
                    wordBoxTag.background.setTint(ContextCompat.getColor(ctx, R.color.colorSecondary))
                }

                wordBox.setOnClickListener { dictionaryViewModel.search(entryModel.entry) }
            } ?: run {
                wordView.text = getString(R.string.wod_error_header)
                definition.text = getString(R.string.wod_error_body)
            }

            definition.visibility = View.VISIBLE
            posView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        })
    }

    private fun setUpHistoryRecyclerView(view: View) {
        val historyContainer: RecyclerView = view.findViewById(R.id.search_history_container)
        searchHistoryAdapter = SearchHistoryAdapter(ctx, this)
        historyContainer.adapter = searchHistoryAdapter
        historyContainer.layoutManager =
            LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
    }



    override fun onResume() {
        super.onResume()
        dictionaryViewModel.updateDailyPick()
    }

    override fun onItemTouch(position: Int) {
        dictionaryViewModel.getHistoryDefinition(position)
    }
}