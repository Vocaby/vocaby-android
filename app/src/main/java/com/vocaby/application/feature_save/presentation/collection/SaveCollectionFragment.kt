package com.vocaby.application.feature_save.presentation.collection

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.core.util.GridItemDecoration
import com.vocaby.application.launchAndRepeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SaveCollectionFragment : Fragment(), SaveCollectionAdapter.Interaction {
    private val saveCollectionViewModel: SaveCollectionViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveCollectionAdapter: SaveCollectionAdapter
    private lateinit var dataObserver: RecyclerView.AdapterDataObserver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_collection, container, false)
        recyclerView = view.findViewById(R.id.save_collection_container)

        setupRecyclerView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            saveCollectionViewModel.saveCollectionState.collectLatest {
                saveCollectionAdapter.submitList(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        saveCollectionAdapter.registerAdapterDataObserver(dataObserver)
    }

    override fun onPause() {
        super.onPause()
        saveCollectionAdapter.unregisterAdapterDataObserver(dataObserver)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireActivity().applicationContext, 2)
        recyclerView.setHasFixedSize(true)
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, requireActivity().applicationContext.resources.displayMetrics)
        recyclerView.addItemDecoration(GridItemDecoration(margin.toInt()))
        saveCollectionAdapter = SaveCollectionAdapter(this)
        dataObserver = object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.smoothScrollToPosition(0)
            }
        }
        recyclerView.adapter = saveCollectionAdapter
    }

    override fun onItemTouch(name: String, id: Int) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
            ).add(R.id.save_collection_fragment_container, SaveCollectionItemsFragment.newInstance(name, id))
            .addToBackStack(null)
            .commit()
    }

    override fun onItemUpdate(collectionName: String, collectionId: Int) {
        saveCollectionViewModel.setCollection(collectionId, collectionName)
    }
}