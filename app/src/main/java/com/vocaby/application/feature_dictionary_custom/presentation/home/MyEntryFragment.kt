package com.vocaby.application.feature_dictionary_custom.presentation.home

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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryViewModel
import com.vocaby.application.feature_dictionary_custom.presentation.entry_builder.EntryBuilderActivity
import com.vocaby.application.states.ItemState
import com.vocaby.searchview.SearchView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MyEntryFragment : Fragment(), CustomEntryAdapter.Interaction {
    private lateinit var entryCountView: TextView
    private lateinit var customEntryAdapter: CustomEntryAdapter
    private lateinit var emptyCard: LinearLayout
    private lateinit var entryCreateDialog: BottomSheetDialog
    private lateinit var entryUpdateDialog: BottomSheetDialog
    private lateinit var entryEditButton: Button
    private lateinit var entryDeleteButton: Button
    private lateinit var entryEdit: EditText
    private lateinit var entryAlert: TextView
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fetchProgress: ProgressBar
    private lateinit var alertDialogBuilder: MaterialAlertDialogBuilder

    private val dictionaryViewModel: DictionaryViewModel by activityViewModels()
    private val entryViewModel: MyEntryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_entry, container, false)
        alertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())
        entryCountView = view.findViewById(R.id.entry_count)
        emptyCard = view.findViewById(R.id.empty_card)
        fetchProgress = view.findViewById(R.id.fetch_progress)
        searchView = view.findViewById(R.id.vocaby_search)
        searchView.apply {
            setOnQueryChangeListener(queryChangeListener)
        }

        setupButtons(view)
        setupEntryBuilderDialog()
        setupEntryUpdateDialog()
        setupRecyclerView(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            // Just suspend on stop, do not unsubscribe onStop
            launch {
                entryViewModel.uiState.collect { state ->
                    when(state) {
                        is CustomEntryUiState.InProgress -> {
                            entryCountView.text = getString(R.string.default_custom_entry_count)
                            fetchProgress.visibility = View.VISIBLE
                            emptyCard.visibility = View.INVISIBLE
                        }
                        is CustomEntryUiState.UpdateCount -> {
                            fetchProgress.visibility = View.INVISIBLE
                            entryCountView.text = state.count
                        }
                        is CustomEntryUiState.ShowEntries -> {
                            customEntryAdapter.submitList(state.entries)
                            fetchProgress.visibility = View.INVISIBLE
                            entryCountView.text = state.count
                            updateEmptyCardVisibility()
                        }
                    }
                }
            }

            launch {
                entryViewModel.uiEvent.collect { event ->
                    when(event) {
                        is CustomEntryUiEvent.ShowAlert -> {
                            entryAlert.text = event.message
                            entryAlert.visibility = View.VISIBLE
                        }
                        is CustomEntryUiEvent.StartEntryBuilder -> {
                            entryEdit.text.clear()
                            entryAlert.visibility = View.INVISIBLE
                            entryCreateDialog.dismiss()

                            openEditor(event.entry, event.position)
                        }
                        is CustomEntryUiEvent.UpdateAdapter -> {
                            when(event.state) {
                                ItemState.DELETE -> {
                                    customEntryAdapter.deleteEntry(event.position)
                                    updateEmptyCardVisibility()
                                }
                                ItemState.ADD -> {
                                    customEntryAdapter.addEntry()
                                    recyclerView.smoothScrollToPosition(0)
                                    updateEmptyCardVisibility()
                                }
                                ItemState.UPDATE -> {
                                    customEntryAdapter.deleteEntry(event.position)
                                    customEntryAdapter.addEntry()
                                    recyclerView.smoothScrollToPosition(0)
                                }
                            }

                            entryUpdateDialog.dismiss()
                            dictionaryViewModel.resetSearchSuggestion()
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.custom_entry_container)
        customEntryAdapter = CustomEntryAdapter( this)
        recyclerView.adapter = customEntryAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity().applicationContext)
    }

    private fun setupButtons(view: View) {
        val addButton = view.findViewById<ExtendedFloatingActionButton>(R.id.add_entry_button)
        addButton.setOnClickListener { entryCreateDialog.show() }
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
            entryViewModel.createCustomEntry(entryEdit.text.toString())
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
                counter.text = s.length.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        }

        entryEdit.addTextChangedListener(textWatcher)
    }

    private fun updateEmptyCardVisibility() {
        if (customEntryAdapter.itemCount > 0 ) emptyCard.visibility = View.INVISIBLE
        else emptyCard.visibility = View.VISIBLE
    }

    private fun setupEntryUpdateDialog() {
        entryUpdateDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        entryUpdateDialog.setContentView(R.layout.dialog_entry_item_action)

        entryEditButton = entryUpdateDialog.findViewById(R.id.edit_entry_button)!!
        entryDeleteButton = entryUpdateDialog.findViewById(R.id.delete_entry_button)!!

        entryUpdateDialog.setOnDismissListener {
            entryEditButton.setOnClickListener(null)
            entryDeleteButton.setOnClickListener(null)
        }
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

    private fun openEditor(entry: String, position: Int = -1) {
        var startEntryBuilderIntent = Intent(requireActivity(), EntryBuilderActivity::class.java)
        startEntryBuilderIntent =
            entryViewModel.addEntryDataToIntent(startEntryBuilderIntent, entry, position)
        entryBuilderActivity.launch(startEntryBuilderIntent)
    }

    override fun onItemUpdate(entry: String, position: Int) {
        entryEditButton.setOnClickListener {
            openEditor(entry, position)
        }

        entryDeleteButton.setOnClickListener {
            entryUpdateDialog.dismiss()
            alertDialogBuilder
                .setTitle("Are you sure you want to delete?")
                .setMessage(entry)
                .setPositiveButton("DELETE") { _, _ ->
                    entryViewModel.removeCustomEntry(entry, position)
                }.setNegativeButton("CANCEL", null).create().show()
        }

        entryUpdateDialog.show()
    }

    override fun onItemTouch(entry: String, position: Int) {
        openEditor(entry, position)
    }
}