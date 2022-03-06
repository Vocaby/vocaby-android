package com.vocaby.application.feature_dictionary.presentation.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.domain.model.EntryModel


class SearchResultsBodyFragment : Fragment() {
    private lateinit var ctx: Context
    private var entryData: EntryModel? = null
    private lateinit var pronunciation: TextView
    private lateinit var pronunciationScroll: LinearLayout
    private lateinit var pronunciationHeader: TextView
    private lateinit var descriptionHeader: TextView
    private lateinit var description: TextView
    private lateinit var noDefinitionAlert: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var entryBox: AppBarLayout
    private lateinit var space: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            entryData = requireArguments().getParcelable(ENTRY_DATA_PARAM)
        }

        ctx = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_results_body, container, false)
        entryBox = view.findViewById(R.id.entry_box)
        pronunciation = view.findViewById(R.id.pronunciation)
        pronunciationHeader = view.findViewById(R.id.pronunciation_header)
        description = view.findViewById(R.id.description)
        descriptionHeader = view.findViewById(R.id.description_header)
        space = view.findViewById(R.id.spacer)
        pronunciationScroll = view.findViewById(R.id.pronunciation_scroll)
        noDefinitionAlert = view.findViewById(R.id.no_definition)
        recyclerView = view.findViewById(R.id.definitions_recycler_container)
        entryData?.let { data ->
            data.firstGroup?.let {
                val definitionsAdapter = DefinitionsAdapter(ctx)
                definitionsAdapter.setWordData(data)

                recyclerView.apply {
                    isEnabled = false
                    adapter = definitionsAdapter
                    layoutManager = LinearLayoutManager(ctx)
                }

                populateView(data)
            } ?: populateNoDefinition()
        } ?: populateNoDefinition()



        return view
    }

    private fun populateView(entryData: EntryModel) {
        if (!entryData.pronunciation.isNullOrEmpty()) {
            pronunciation.visibility = View.VISIBLE
            pronunciationHeader.visibility = View.VISIBLE
            pronunciation.text = entryData.pronunciation
        }

        if (!entryData.description.isNullOrEmpty()) {
            description.visibility = View.VISIBLE
            descriptionHeader.visibility = View.VISIBLE
            description.text = entryData.description
        }

        if (!entryData.pronunciation.isNullOrEmpty() && !entryData.description.isNullOrEmpty()) {
            space.visibility = View.VISIBLE
        } else if (entryData.pronunciation.isNullOrEmpty() && entryData.description.isNullOrEmpty()) {
            entryBox.setExpanded(false)
        }

        noDefinitionAlert.visibility = View.GONE
    }

    private fun populateNoDefinition() {
        pronunciation.visibility = View.GONE
        pronunciationHeader.visibility = View.GONE
        recyclerView.visibility = View.GONE
        space.visibility = View.GONE
        noDefinitionAlert.visibility = View.VISIBLE
    }

    companion object {
        private const val ENTRY_DATA_PARAM = "entryData"
        fun newInstance(entryData: EntryModel?): SearchResultsBodyFragment {
            val fragment = SearchResultsBodyFragment()
            val args = Bundle()
            args.putParcelable(ENTRY_DATA_PARAM, entryData)
            fragment.arguments = args
            return fragment
        }
    }
}