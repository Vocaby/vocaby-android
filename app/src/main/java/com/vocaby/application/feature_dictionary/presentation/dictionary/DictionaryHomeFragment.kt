package com.vocaby.application.feature_dictionary.presentation.dictionary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.application.R
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.HorizontalItemDecoration
import com.vocaby.application.core.util.RecyclerViewPagerDecoration
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder.EntryBuilderActivity
import com.vocaby.application.feature_dictionary_custom.presentation.home.MyEntryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class DictionaryHomeFragment : Fragment(), SearchHistoryAdapter.OnItemTouchListener, EodListAdapter.Interaction {
    private lateinit var ctx: Context
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var emptyCard: TextView
    private lateinit var entryCreateDialog: BottomSheetDialog
    private lateinit var entryEdit: EditText
    private lateinit var entryAlert: TextView
    private lateinit var eodRecyclerView: RecyclerView
    private lateinit var eodAdapter: EodListAdapter
    private lateinit var eodPlaceholder: ShimmerFrameLayout
    private lateinit var eodAlert: TextView
    private lateinit var prevPickRecyclerView: RecyclerView
    private lateinit var prevPickAdapter: PrevPickAdapter
    private lateinit var prevPickAlert: TextView

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
        emptyCard = view.findViewById(R.id.empty_card)
        eodRecyclerView = view.findViewById(R.id.eod_recycler_view)
        eodPlaceholder = view.findViewById(R.id.placeholder_eod)
        eodAlert = view.findViewById(R.id.daily_pick_alert)
        prevPickRecyclerView = view.findViewById(R.id.previous_selection_recycler_view)
        prevPickAlert = view.findViewById(R.id.previous_selection_empty_text)

        setupEntryBuilderDialog()
        setupButtons(view)
        setUpHistoryRecyclerView(view)
        setupEodRecyclerView()
        setupPrevPickRecyclerView()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            launch {
                collectDailyPick()
            }

            launch {
                collectPrevPick()
            }

            launch {
                collectSearchHistory()
            }

            launch {
                collectUiEvent()
            }
        }
    }

    private suspend fun collectPrevPick() {
        dictionaryViewModel.prevPicks.collect { prevPicks ->
            if (!prevPicks.isNullOrEmpty()) {
                prevPickAlert.visibility = View.INVISIBLE
                prevPickAdapter.submitList(prevPicks)
            }
        }
    }

    private suspend fun collectUiEvent() {
        dictionaryViewModel.uiEvent.collect { event ->
            when(event) {
                is DictionaryHomeUiEvent.OpenEntryBuilder -> {
                    val intent = Intent(requireActivity(), EntryBuilderActivity::class.java)
                    intent.putExtra(Constants.ENTRY_KEY, event.entry)
                    entryBuilderActivity.launch(intent)
                }
                is DictionaryHomeUiEvent.ShowAlert -> {
                    entryAlert.text = event.message
                    entryAlert.visibility = View.VISIBLE
                }
            }
        }
    }

    private suspend fun collectSearchHistory() {
        dictionaryViewModel.searchHistory.collectLatest { searchHistory ->
            searchHistory?.let {
                searchHistoryAdapter.updateSearchHistory(searchHistory)

                if (searchHistory.isNotEmpty()) emptyCard.visibility = View.INVISIBLE
                else emptyCard.visibility = View.VISIBLE
            }
        }
    }

    private suspend fun collectDailyPick() {
        dictionaryViewModel.dailyPick.collectLatest { dailyPickState ->
            when (dailyPickState) {
                is DailyPickState.InProgress -> {
                    eodAlert.visibility = View.VISIBLE
                    eodPlaceholder.visibility = View.VISIBLE
                }
                is DailyPickState.Picked -> {
                    eodAlert.visibility = View.INVISIBLE
                    eodPlaceholder.visibility = View.GONE

                    val show = AlphaAnimation(0.0f, 1.0f)
                    show.duration = 300
                    eodRecyclerView.startAnimation(show)
                    eodRecyclerView.visibility = View.VISIBLE

                    eodAdapter.submitList(dailyPickState.picks)
                }
            }
        }
    }

    private fun setupEodRecyclerView() {
        eodAdapter = EodListAdapter(ctx, this)
        val layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        eodRecyclerView.layoutManager = layoutManager
        eodRecyclerView.addItemDecoration(RecyclerViewPagerDecoration(
            ContextCompat.getColor(ctx, R.color.colorPrimary),
            ContextCompat.getColor(ctx, R.color.gray)
        ))
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, ctx.resources.displayMetrics)
        eodRecyclerView.addItemDecoration(HorizontalItemDecoration(margin.toInt()))
        eodRecyclerView.adapter = eodAdapter

        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(eodRecyclerView)
    }

    private fun setupPrevPickRecyclerView() {
        prevPickAdapter = PrevPickAdapter(ctx)
        prevPickRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        prevPickRecyclerView.adapter = prevPickAdapter
    }

    private fun setupButtons(view: View) {
        val editDictionary = view.findViewById<AppCompatButton>(R.id.edit_dictionary_button)
        editDictionary.setOnClickListener {
            val intent = Intent(requireActivity(), MyEntryActivity::class.java)
            myEntryActivity.launch(intent)
        }

        val addEntry = view.findViewById<AppCompatButton>(R.id.add_entry_button)
        addEntry.setOnClickListener {
            entryCreateDialog.show()
        }
    }

    private fun setupEntryBuilderDialog() {
        entryCreateDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        entryCreateDialog.setContentView(R.layout.dialog_custom_entry_create)
        entryCreateDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        entryEdit = entryCreateDialog.findViewById(R.id.entry_edit)!!
        entryAlert = entryCreateDialog.findViewById(R.id.entry_header_alert)!!

        val createButton = entryCreateDialog.findViewById<Button>(R.id.dialog_entry_create_button)

        createButton?.setText(R.string.create)
        createButton?.setOnClickListener {
            dictionaryViewModel.createCustomEntry(entryEdit.text.toString())
        }

        val counter = entryCreateDialog.findViewById<TextView>(R.id.counter)!!
        entryEdit.addTextChangedListener {
            counter.text = it?.length.toString()
        }

        // Clear content on show
        entryCreateDialog.setOnShowListener {
            entryEdit.clearFocus()
            entryEdit.text?.clear()
            entryAlert.visibility = View.INVISIBLE
        }
    }

    private fun setUpHistoryRecyclerView(view: View) {
        val historyContainer: RecyclerView = view.findViewById(R.id.search_history_container)
        searchHistoryAdapter = SearchHistoryAdapter(ctx, this)
        historyContainer.adapter = searchHistoryAdapter
        historyContainer.layoutManager =
            LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
    }

    private val entryBuilderActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        entryCreateDialog.dismiss()
        dictionaryViewModel.handleResult(result)
    }

    private val myEntryActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        dictionaryViewModel.handleResult(result)
    }


    override fun onItemTouch(position: Int) {
        dictionaryViewModel.getHistoryDefinition(position)
    }

    override fun onItemTouch(entry: String) {
        dictionaryViewModel.search(entry)
    }
}