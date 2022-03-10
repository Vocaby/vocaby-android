package com.vocaby.application.feature_dictionary.presentation.dictionary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.application.R
import com.vocaby.application.core.Constants
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder.EntryBuilderActivity
import com.vocaby.application.feature_dictionary_custom.presentation.home.MyEntryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DictionaryHomeFragment : Fragment(), SearchHistoryAdapter.OnItemTouchListener {
    private lateinit var ctx: Context
    private lateinit var wordView: TextView
    private lateinit var typeView: TextView
    private lateinit var definition: TextView
    private lateinit var sentence: TextView
    private lateinit var wordBox: View
    private lateinit var wordBoxTag: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var emptyCard: TextView
    private lateinit var entryCreateDialog: BottomSheetDialog
    private lateinit var entryEdit: EditText
    private lateinit var entryAlert: TextView

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
        // Random Entry of the Day
        wordView = view.findViewById(R.id.entry_header)
        typeView = view.findViewById(R.id.type)
        definition = view.findViewById(R.id.card_definition)
        sentence = view.findViewById(R.id.card_sentence)
        wordBox = view.findViewById(R.id.word_box)
        progressBar = view.findViewById(R.id.randomword_progress)
        wordBoxTag = view.findViewById(R.id.word_box_tag)

        progressBar.visibility = View.VISIBLE
        definition.visibility = View.GONE
        sentence.visibility = View.GONE
        typeView.visibility = View.GONE

        emptyCard = view.findViewById(R.id.empty_card)

        setupEntryBuilderDialog()
        setupButtons(view)
        setUpHistoryRecyclerView(view)
        dictionaryViewModel.updateDailyPick()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            dictionaryViewModel.dailyPick.collectLatest { dailyPickState ->
                when (dailyPickState) {
                    is DailyPickState.InProgress -> {
                        wordView.visibility = View.INVISIBLE
                        wordBoxTag.visibility = View.GONE
                        definition.visibility = View.GONE
                        typeView.visibility = View.GONE
                        sentence.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                    }
                    is DailyPickState.Picked -> {
                        wordView.visibility = View.VISIBLE
                        wordBoxTag.visibility = View.VISIBLE
                        definition.visibility = View.VISIBLE
                        typeView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE

                        val dailyPick = dailyPickState.pick
                        wordView.text = dailyPick.entry
                        typeView.text =  dailyPick.type
                        definition.text = dailyPick.definition

                        dailyPick.example?.let {
                            if (it.isNotEmpty()) {
                                sentence.visibility = View.VISIBLE
                                sentence.text = it
                            }
                        }

                        if (dailyPick.random) {
                            wordBoxTag.text = getString(R.string.wod_random_pick)
                            wordBoxTag.setTextColor(ContextCompat.getColor(ctx, R.color.colorHeadline))
                            wordBoxTag.background.setTint(ContextCompat.getColor(ctx, R.color.colorHeadlineSoft))
                        } else {
                            wordBoxTag.text = getString(R.string.wod_our_pick)
                            wordBoxTag.setTextColor(ContextCompat.getColor(ctx, R.color.colorPrimaryAccent))
                            wordBoxTag.background.setTint(ContextCompat.getColor(ctx, R.color.colorSecondary))
                        }

                        wordBox.setOnClickListener { dictionaryViewModel.search(dailyPick.entry) }
                    }
                }
            }
        }

        launchAndRepeatWithViewLifecycle {
            dictionaryViewModel.searchHistory.collectLatest { searchHistory ->
                searchHistory?.let {
                    searchHistoryAdapter.updateSearchHistory(searchHistory)

                    if (searchHistory.isNotEmpty()) emptyCard.visibility = View.INVISIBLE
                    else emptyCard.visibility = View.VISIBLE
                }
            }
        }

        launchAndRepeatWithViewLifecycle {
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
    }

    private fun setupButtons(view: View) {
        val editDictionary = view.findViewById<AppCompatButton>(R.id.edit_dictionary_button)
        editDictionary.setOnClickListener {
            startActivity(Intent(requireActivity(), MyEntryActivity::class.java))
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
        entryEdit = entryCreateDialog.findViewById(R.id.entry_edit)!!
        entryAlert = entryCreateDialog.findViewById(R.id.entry_header_alert)!!

        val createButton = entryCreateDialog.findViewById<Button>(R.id.dialog_entry_create_button)

        createButton?.setText(R.string.create)
        createButton?.setOnClickListener {
            dictionaryViewModel.createCustomEntry(entryEdit.text.toString())
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
            result: ActivityResult? ->
        entryCreateDialog.dismiss()
    }


    override fun onItemTouch(position: Int) {
        dictionaryViewModel.getHistoryDefinition(position)
    }
}