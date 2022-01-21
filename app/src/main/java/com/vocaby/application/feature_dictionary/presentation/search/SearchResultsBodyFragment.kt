package com.vocaby.application.feature_dictionary.presentation.search

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.presentation.dictionary.DefinitionsAdapter


class SearchResultsBodyFragment : Fragment() {
    private lateinit var ctx: Context
    private var entryData: EntryModel? = null
    private lateinit var header: TextView
    private lateinit var pronunciation: TextView
    private lateinit var pronunciationScroll: HorizontalScrollView
    private lateinit var recyclerView: RecyclerView
    private lateinit var entryBox: LinearLayout

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
        entryBox = view.findViewById(R.id.entry_box)
        header = view.findViewById(R.id.entry_header)
        pronunciation = view.findViewById(R.id.pronunciation)
        pronunciationScroll = view.findViewById(R.id.pronunciation_scroll)
        recyclerView = view.findViewById(R.id.definitions_recycler_container)
        entryData?.let { data ->
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
            pronunciationScroll.visibility = View.VISIBLE
            pronunciation.text = entryData.pronunciation
        } else {
            entryBox.setPadding(0, -ctx.resources.getDimensionPixelSize(R.dimen.header_gap), 0, 0)
            entryBox.gravity = Gravity.CENTER
        }
    }

    private fun populateNoDefinition() {
        header.text = resources.getString(R.string.no_definition_found)
        entryBox.setPadding(0, -ctx.resources.getDimensionPixelSize(R.dimen.header_gap), 0, 0)
        entryBox.gravity = Gravity.CENTER
        pronunciationScroll.visibility = View.GONE
        recyclerView.visibility = View.GONE
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