package com.vocaby.application.feature_save.presentation.save

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.core.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CollectionItemsFragment : Fragment(), SaveListAdapter.Interaction {
    private lateinit var ctx: Context
    private lateinit var savesAdapter: SaveListAdapter
    private lateinit var savesCount: TextView
    private lateinit var emptyCard: LinearLayout
    private val saveViewModel: SaveViewModel by viewModels()

    companion object {
        const val COLLECTION_ALL_PARAM = "showAll"
        @JvmStatic
        fun newInstance(showAll: Boolean): CollectionItemsFragment {
            val fragment = CollectionItemsFragment()
            val args = Bundle()
            args.putBoolean(COLLECTION_ALL_PARAM, showAll)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_collection_items, container, false)
        emptyCard = view.findViewById(R.id.empty_card)

        val closeButton: Button = view.findViewById(R.id.back_button)
        closeButton.setOnClickListener { requireActivity().onBackPressed() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        lifecycleScope.launchWhenStarted {
            saveViewModel.savedWords.collectLatest { saves ->
                if (saves.isNotEmpty()) emptyCard.visibility = View.INVISIBLE
                else emptyCard.visibility = View.VISIBLE

                savesAdapter.submitList(saves)
            }
        }

//        lifecycleScope.launchWhenStarted {
//            saveViewModel.savesCount.collectLatest { count ->
//                savesCount.text = count.toString()
//            }
//        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.saves_container)
        savesAdapter = SaveListAdapter(requireActivity(), this)
        recyclerView.adapter = savesAdapter
        recyclerView.layoutManager = LinearLayoutManager(ctx)
    }

    override fun onItemDelete(entry: String) {
        saveViewModel.removeSaveItem(entry)
    }

    override fun onItemTouch(entry: String) {
        (requireActivity() as MainActivity).showDefinition(entry)
    }
}