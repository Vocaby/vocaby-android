package com.vocaby.application.feature_dictionary_custom.presentation.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.vocaby.application.R
import com.vocaby.application.core.states.ItemState
import com.vocaby.application.feature_dictionary.presentation.dictionary.DictionaryViewModel
import com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder.EntryBuilderActivity
import com.vocaby.application.feature_dictionary_custom.presentation.type.TypeManagementActivity
import com.vocaby.vocabywidgets.searchview.SearchView
import com.vocaby.vocabywidgets.searchview.suggestions.model.SearchSuggestion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// TODO: Create custom swipe refresh layout for entry pagination
@AndroidEntryPoint
class MyEntryFragment : Fragment(), CustomEntryAdapter.Interaction {
    private lateinit var customEntryAdapter: CustomEntryAdapter
    private lateinit var addFab: ExtendedFloatingActionButton
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
    private lateinit var counter: TextView
    private lateinit var appBarLayout: AppBarLayout

    private val dictionaryViewModel: DictionaryViewModel by activityViewModels()
    private val entryViewModel: MyEntryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_entry, container, false)
        alertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())
        emptyCard = view.findViewById(R.id.empty_card)
        fetchProgress = view.findViewById(R.id.fetch_progress)
        searchView = view.findViewById(R.id.vocaby_search)
        appBarLayout = view.findViewById(R.id.app_layout)
        searchView.apply {
            setOnQueryChangeListener(queryChangeListener)
            setOnSearchListener(searchListener)
        }

        val typeManagementButton = view.findViewById<Button>(R.id.type_management_button)
        typeManagementButton.setOnClickListener {
            val intent = Intent(requireActivity(), TypeManagementActivity::class.java)
            startActivity(intent)
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
            // Just suspend and do not unsubscribe onStop
            // onResume is called when coming back from entry builder,
            // unnecessarily resubmitting the list
            launch {
                entryViewModel.entryListState.collect { state ->
                    when (state) {
                        is CustomEntryListState.InProgress -> {
                            recyclerView.visibility = View.INVISIBLE
                            fetchProgress.visibility = View.VISIBLE
                            emptyCard.visibility = View.INVISIBLE
                        }
                        is CustomEntryListState.UpdateEntries -> {
                            fetchProgress.visibility = View.INVISIBLE
                            customEntryAdapter.submitList(state.entries)
                            recyclerView.visibility = View.VISIBLE
                            updateEmptyCardVisibility()
                            recyclerView.scrollToPosition(0)
                            appBarLayout.setExpanded(true)
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
                        is CustomEntryUiEvent.OpenEntryBuilder -> {
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
                        is CustomEntryUiEvent.ResetFilter -> {
                            searchView.clearQuery()
                        }
                        is CustomEntryUiEvent.ShowMoreProgress -> {
                            customEntryAdapter.addEntryLast()
                            if (event.scroll) recyclerView.scrollToPosition(customEntryAdapter.itemCount - 1)
                        }
                        is CustomEntryUiEvent.AddMoreEntries -> {
                            customEntryAdapter.addEntryRange(event.low, event.high)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.custom_entry_container)
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1)
                    && newState==RecyclerView.SCROLL_STATE_IDLE
                    && addFab.translationY > 100f) {
                    val show = AlphaAnimation(0f, 1.0f)
                    show.duration = 300
                    addFab.startAnimation(show)
                    addFab.translationY = 0f
                }
            }
        })

        customEntryAdapter = CustomEntryAdapter( this)
        recyclerView.adapter = customEntryAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity().applicationContext)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    entryViewModel.loadMoreEntries(true)
                }
            }
        })
    }

    private fun setupButtons(view: View) {
        addFab = view.findViewById(R.id.add_entry_button)
        addFab.setOnClickListener { entryCreateDialog.show() }
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

        counter = entryCreateDialog.findViewById(R.id.character_counter)!!
        entryEdit.addTextChangedListener(textWatcher)
    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            counter.text = s.length.toString()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    private fun updateEmptyCardVisibility() {
        if (customEntryAdapter.itemCount > 0) emptyCard.visibility = View.INVISIBLE
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
            entryViewModel.watchFilter(oldQuery, newQuery)
        }
    }

    private val searchListener = object: SearchView.OnSearchListener {
        override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {  }
        override fun onSearchAction(currentQuery: String) {
            entryViewModel.filterEntries(currentQuery)
            if (addFab.translationY != 0f) {
                val show = AlphaAnimation(0f, 1.0f)
                show.duration = 300
                addFab.startAnimation(show)
                addFab.translationY = 0f
            }
        }
    }

    private fun openEditor(entry: String, position: Int = -1) {
        var startEntryBuilderIntent = Intent(requireActivity(), EntryBuilderActivity::class.java)
        startEntryBuilderIntent =
            entryViewModel.addEntryDataToIntentForBuilder(startEntryBuilderIntent, entry, position)
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

    override fun onDestroy() {
        recyclerView.clearOnScrollListeners()
        entryEdit.removeTextChangedListener(textWatcher)
        super.onDestroy()
    }
}