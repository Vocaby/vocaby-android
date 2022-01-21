package com.vocaby.application.feature_save.presentation.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.feature_save.presentation.save.CollectionItemsFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SaveCollectionFragment : Fragment(), SaveCollectionAdapter.Interaction {
    private val saveCollectionViewModel: SaveCollectionViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveCollectionAdapter: SaveCollectionAdapter

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

        lifecycleScope.launchWhenStarted {
            saveCollectionViewModel.saveCollectionState.collectLatest {
                saveCollectionAdapter.submitList(it)
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireActivity().applicationContext, 2)
        saveCollectionAdapter = SaveCollectionAdapter(this)
        recyclerView.adapter = saveCollectionAdapter
    }

    override fun onItemTouch(allSaves: Boolean) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
            ).add(R.id.save_collection_fragment_container, CollectionItemsFragment.newInstance(allSaves))
            .addToBackStack(null)
            .commit()
    }
}