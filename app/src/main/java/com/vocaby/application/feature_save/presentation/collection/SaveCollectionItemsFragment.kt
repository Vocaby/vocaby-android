package com.vocaby.application.feature_save.presentation.collection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.vocaby.application.R
import com.vocaby.application.core.presentation.MainActivity
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.feature_save.presentation.save.SaveListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs


@AndroidEntryPoint
class SaveCollectionItemsFragment : Fragment(), SaveListAdapter.Interaction {
    private lateinit var ctx: Context
    private lateinit var savesAdapter: SaveListAdapter
    private lateinit var collectionHeader: TextView
    private lateinit var collectionHeaderSmall: TextView
    private lateinit var collectionSaveCounter: TextView
    private lateinit var collectionSaveCounterText: TextView
    private lateinit var emptyCard: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataObserver: RecyclerView.AdapterDataObserver
    private lateinit var appLayout: AppBarLayout
    private lateinit var showAnimation: AnimationSet
    private lateinit var hideAnimation: AnimationSet

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

        showAnimation = AnimationSet(true)
        val translateAnimation1 = TranslateAnimation(0f, 0f, 20f, 0f)
        translateAnimation1.duration = 300
        val alphaAnimation1 = AlphaAnimation(0f, 1.0f)
        alphaAnimation1.duration = 300
        showAnimation.addAnimation(translateAnimation1)
        showAnimation.addAnimation(alphaAnimation1)

        hideAnimation = AnimationSet(true)
        val translateAnimation2 = TranslateAnimation(0f, 0f, 0f, 20f)
        translateAnimation2.duration = 150
        val alphaAnimation2 = AlphaAnimation(1.0f, 0f)
        alphaAnimation2.duration = 150
        hideAnimation.addAnimation(translateAnimation2)
        hideAnimation.addAnimation(alphaAnimation2)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_collection_items, container, false)
        emptyCard = view.findViewById(R.id.empty_card)
        collectionHeader = view.findViewById(R.id.collection_header)
        collectionHeaderSmall = view.findViewById(R.id.collection_header_small)
        collectionSaveCounter = view.findViewById(R.id.save_counter)
        collectionSaveCounterText = view.findViewById(R.id.save_counter_text)

        collectionHeader.text = arguments?.getString(COLLECTION_NAME_PARAM)
        collectionHeaderSmall.text = arguments?.getString(COLLECTION_NAME_PARAM)

        appLayout = view.findViewById(R.id.app_layout)

        val closeButton: Button = view.findViewById(R.id.back_button)
        closeButton.setOnClickListener { requireActivity().onBackPressed() }

        appLayout.addOnOffsetChangedListener(offsetListener)

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
                collectionSaveCounter.text = count.toString()
                collectionSaveCounterText.text = if (count > 1) "Items" else "Item"
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.saves_container)
        savesAdapter = SaveListAdapter(requireActivity(), this, true)
        recyclerView.adapter = savesAdapter
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        dataObserver = object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.smoothScrollToPosition(0)
            }
        }

        savesAdapter.registerAdapterDataObserver(dataObserver)
    }

    private val offsetListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (abs(verticalOffset) == appBarLayout.totalScrollRange) {
                if (collectionHeaderSmall.visibility == View.INVISIBLE) {
                    collectionHeaderSmall.visibility = View.VISIBLE
                    collectionHeaderSmall.startAnimation(showAnimation)
                }
            } else {
                if (collectionHeaderSmall.visibility == View.VISIBLE) {
                    collectionHeaderSmall.visibility = View.INVISIBLE
                    collectionHeaderSmall.startAnimation(hideAnimation)
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        savesAdapter.unregisterAdapterDataObserver(dataObserver)
    }

    override fun onItemDelete(entry: String) {
        collectionItemsViewModel.removeSaveItem(entry)
    }

    override fun onItemTouch(entry: String) {
        (requireActivity() as MainActivity).showDefinition(entry)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appLayout.removeOnOffsetChangedListener(offsetListener)
    }
}