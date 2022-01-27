package com.vocaby.application.feature_dictionary_custom.presentation.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.application.R
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryViewModel
import com.vocaby.application.feature_dictionary_custom.presentation.adapter.CustomEntryAdapter
import com.vocaby.application.feature_dictionary_custom.presentation.viewmodel.MyEntryViewModel
import com.vocaby.application.states.ItemState
import com.vocaby.application.states.UserInputState
import com.vocaby.searchview.SearchView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyEntryFragment : Fragment(), CustomEntryAdapter.Interaction {
    private lateinit var ctx: Context
    private lateinit var entryCountView: TextView
    private lateinit var customEntryAdapter: CustomEntryAdapter
    private lateinit var emptyCard: LinearLayout
    private lateinit var entryCreateDialog: BottomSheetDialog
    private lateinit var entryEdit: EditText
    private lateinit var entryAlert: TextView
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fetchProgress: ProgressBar

    private val dictionaryViewModel: DictionaryViewModel by activityViewModels()
    private val entryViewModel: MyEntryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = requireActivity().applicationContext
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_entry, container, false)
        entryCountView = view.findViewById(R.id.entry_count)
        emptyCard = view.findViewById(R.id.empty_card)
        fetchProgress = view.findViewById(R.id.fetch_progress)

        searchView = view.findViewById(R.id.vocaby_search)
        searchView.apply {
            setOnQueryChangeListener(queryChangeListener)
        }

        setupButtons(view)
        setupEntryBuilderDialog()
        setupRecyclerView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // On config change
        if (savedInstanceState != null) {
            entryViewModel.reinitializeEntries()
        }

        entryViewModel.entries.observe(viewLifecycleOwner) { fetchState ->
            when (fetchState) {
                is GenericState.Success -> {
                    fetchProgress.visibility = View.GONE
                    customEntryAdapter.submitList(fetchState.data)
                    updateEmptyCardVisibility()
                }

                is GenericState.InProgress -> {
                    fetchProgress.visibility = View.VISIBLE
                    emptyCard.visibility = View.INVISIBLE
                }

                is GenericState.Error -> {
                    fetchProgress.visibility = View.GONE
                }
            }
        }

        entryViewModel.customEntryCount.observe(viewLifecycleOwner) { count ->
            entryCountView.text = Formatter.cleanNumber(count)
        }

        entryViewModel.entryResult.observe(viewLifecycleOwner) { itemPayload ->
            when (itemPayload.state) {
                ItemState.DELETE -> customEntryAdapter.deleteEntry(itemPayload.payload)
                ItemState.ADD -> {
                    customEntryAdapter.addEntry()
                    recyclerView.smoothScrollToPosition(0)
                }
                ItemState.UPDATE -> {
                    customEntryAdapter.deleteEntry(itemPayload.payload)
                    customEntryAdapter.addEntry()
                    recyclerView.smoothScrollToPosition(0)
                }
            }

            updateEmptyCardVisibility()
            dictionaryViewModel.resetSearchSuggestion()
        }

        entryViewModel.entryCreationState.observe(viewLifecycleOwner) { input ->
            when(input) {
                is UserInputState.EmptyInput -> {
                    entryAlert.text = getString(R.string.custom_entry_header_empty)
                    entryAlert.visibility = View.VISIBLE
                }
                is UserInputState.InvalidInput -> {
                    entryAlert.text = getString(R.string.custom_entry_header_invalid)
                    entryAlert.visibility = View.VISIBLE
                }
                is UserInputState.LongInput -> {
                    entryAlert.text = getString(R.string.custom_entry_header_long)
                    entryAlert.visibility = View.VISIBLE
                }
                is UserInputState.SameInput -> {
                    entryAlert.text = getString(R.string.custom_entry_exists)
                    entryAlert.visibility = View.VISIBLE
                }
                is UserInputState.Valid -> {
                    entryEdit.text.clear()
                    entryAlert.visibility = View.INVISIBLE
                    entryCreateDialog.dismiss()

                    var startEntryBuilderIntent =
                        Intent(requireActivity(), EntryBuilderActivity::class.java)
                    startEntryBuilderIntent = entryViewModel.addEntryDataToIntent(
                        startEntryBuilderIntent,
                        input.data
                    )

                    entryBuilderActivity.launch(startEntryBuilderIntent)
                }
                else -> {}
            }
        }
    }

    private fun updateEmptyCardVisibility() {
        if (customEntryAdapter.itemCount > 0 ) emptyCard.visibility = View.INVISIBLE
        else emptyCard.visibility = View.VISIBLE
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.custom_entry_container)
        customEntryAdapter = CustomEntryAdapter(requireActivity(), this)
        recyclerView.adapter = customEntryAdapter
        recyclerView.layoutManager = LinearLayoutManager(ctx)
    }

    private fun setupButtons(view: View) {
        val addButton = view.findViewById<Button>(R.id.add_entry_button)
        addButton.setOnClickListener { entryCreateDialog.show() }
    }

    private fun setupEntryBuilderDialog() {
        entryCreateDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        entryCreateDialog.setContentView(R.layout.custom_entry_create_dialog)
        entryEdit = entryCreateDialog.findViewById(R.id.entry_edit)!!
        entryAlert = entryCreateDialog.findViewById(R.id.entry_header_alert)!!

        val createButton = entryCreateDialog.findViewById<Button>(R.id.dialog_entry_create_button)

        createButton?.setText(R.string.create)
        createButton?.setOnClickListener {
            entryViewModel.createCustomEntry(entryEdit.text.toString())
            entryViewModel.resetSelections()
        }

        // Clear content on show
        entryCreateDialog.setOnShowListener {
            entryEdit.clearFocus()
            entryEdit.text?.clear()
            entryAlert.visibility = View.INVISIBLE
        }

        val counter = entryCreateDialog.findViewById<TextView>(R.id.character_counter)!!
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                counter.text = count.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        }

        entryEdit.addTextChangedListener(textWatcher)
    }

    private val entryBuilderActivity = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult? ->
        entryViewModel.handleResult(result!!)
    }

    private val queryChangeListener = object: SearchView.OnQueryChangeListener {
        override fun onSearchTextChanged(oldQuery: String, newQuery: String) {
            recyclerView.smoothScrollToPosition(0)
            entryViewModel.filterEntries(newQuery)
        }

    }

    override fun onItemDelete(entry: String, position: Int) {
        entryViewModel.removeCustomEntry(entry, position)
    }

    override fun onItemEdit(entry: String, position: Int) {
        var startEntryBuilderIntent = Intent(requireActivity(), EntryBuilderActivity::class.java)
        startEntryBuilderIntent =
            entryViewModel.addEntryDataToIntent(startEntryBuilderIntent, entry, position)
        entryBuilderActivity.launch(startEntryBuilderIntent)
    }
}