package com.vocaby.application.feature_save.presentation.save

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.core.presentation.MainActivity
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collectLatest

class AllSavesFragment : Fragment(), SaveListAdapter.Interaction {
    private lateinit var recyclerView: RecyclerView
    private lateinit var savesAdapter: SaveListAdapter
    private lateinit var emptyCard: LinearLayout
    private lateinit var dataObserver: RecyclerView.AdapterDataObserver

    private val savesViewModel: SaveViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saves_all, container, false)
        emptyCard = view.findViewById(R.id.empty_card)
        recyclerView = view.findViewById(R.id.all_saves_container)
        savesAdapter = SaveListAdapter(requireActivity(), this)
        recyclerView.adapter = savesAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity().applicationContext)
        dataObserver = object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.smoothScrollToPosition(0)
            }
        }
        savesAdapter.registerAdapterDataObserver(dataObserver)

        launchAndRepeatWithViewLifecycle {
            savesViewModel.savedWords.collectLatest { saves ->
                if (saves.isNotEmpty()) emptyCard.visibility = View.INVISIBLE
                else emptyCard.visibility = View.VISIBLE

                savesAdapter.submitList(saves)
            }
        }

        return view
    }


    override fun onItemDelete(entry: String) {
        savesViewModel.removeSaveItem(entry)
    }

    override fun onItemTouch(entry: String) {
        (requireActivity() as MainActivity).showDefinition(entry)
    }

    override fun onDestroy() {
        super.onDestroy()
        savesAdapter.unregisterAdapterDataObserver(dataObserver)
    }
}