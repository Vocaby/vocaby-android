package com.vocaby.application.feature_save.presentation.collection

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vocaby.application.R
import com.vocaby.application.core.presentation.MainActivity
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.feature_save.presentation.save.SaveListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SaveCollectionItemsFragment : Fragment(), SaveListAdapter.Interaction {
    private lateinit var ctx: Context
    private lateinit var savesAdapter: SaveListAdapter
    private lateinit var collectionHeader: TextView
    private lateinit var collectionSaveCounter: TextView
    private lateinit var emptyCard: LinearLayout
    private val collectionItemsViewModel: SaveCollectionItemsViewModel by viewModels()

    companion object {
        const val COLLECTION_NAME_PARAM = "COLLECTION"
        const val COLLECTION_ID_PARAM = "COLLECTION_ID"


        @JvmStatic
        fun newInstance(name: String, id: Int): SaveCollectionItemsFragment {
            val fragment = SaveCollectionItemsFragment()
            val args = Bundle()
            args.putString(COLLECTION_NAME_PARAM, name)
            args.putInt(COLLECTION_ID_PARAM, id)
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
        collectionHeader = view.findViewById(R.id.collection_header)
        collectionSaveCounter = view.findViewById(R.id.save_counter)
        collectionHeader.text = arguments?.getString(COLLECTION_NAME_PARAM)

        val closeButton: Button = view.findViewById(R.id.back_button)
        closeButton.setOnClickListener { requireActivity().onBackPressed() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        launchAndRepeatWithViewLifecycle {
            collectionItemsViewModel.savedWords.collectLatest { saves ->
                if (saves.isNotEmpty()) emptyCard.visibility = View.INVISIBLE
                else emptyCard.visibility = View.VISIBLE

                savesAdapter.submitList(saves)
            }
        }

        launchAndRepeatWithViewLifecycle {
            collectionItemsViewModel.saveCount.collectLatest { count ->
                val text= "$count Saved"
                collectionSaveCounter.text = text
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.saves_container)
        savesAdapter = SaveListAdapter(requireActivity(), this, true)
        recyclerView.adapter = savesAdapter
        recyclerView.layoutManager = LinearLayoutManager(ctx)
    }

    override fun onItemDelete(entry: String) {
        collectionItemsViewModel.removeSaveItem(entry)
    }

    override fun onItemTouch(entry: String) {
        (requireActivity() as MainActivity).showDefinition(entry)
    }
}