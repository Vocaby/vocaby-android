package com.vocaby.app.ui.dictionary

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.app.R
import com.vocaby.app.adapters.DefinitionsAdapter
import com.vocaby.app.models.dictionary.EntryModel

class SearchResultsBodyFragment : Fragment() {
    private lateinit var ctx: Context
    private var entryData: EntryModel? = null
    private lateinit var header: TextView
    private lateinit var pronunciation: TextView

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search_results_body, container, false)
        header = view.findViewById(R.id.word_header)
        pronunciation = view.findViewById(R.id.pronunciation)

        entryData?.let { data ->
            val recyclerView: RecyclerView = view.findViewById(R.id.definitions_recycler_container)
            val definitionsAdapter = DefinitionsAdapter(ctx)
            definitionsAdapter.setWordData(data)

            recyclerView.apply {
                isEnabled = false
                adapter = definitionsAdapter
                layoutManager = LinearLayoutManager(ctx)

                if (data.definitionGroups.size > 1)
                    recyclerView.addItemDecoration(DividerItemDecoration(ctx, LinearLayoutManager.VERTICAL))
            }

            populateView(data)
        } ?: populateNoDefinition()

        return view
    }

    private fun populateView(entryData: EntryModel) {
        header.text = entryData.entry
        if (!entryData.pronunciation.isNullOrEmpty()) {
            pronunciation.visibility = View.VISIBLE
            pronunciation.text = entryData.pronunciation
        }
    }

    private fun populateNoDefinition() {
        header.text = resources.getString(R.string.no_definition_found)
        pronunciation.visibility = View.GONE
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