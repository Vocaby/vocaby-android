package com.vocaby.application.feature_dictionary_custom.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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

@AndroidEntryPoint
class MyEntryActivity : AppCompatActivity(), CustomEntryAdapter.Interaction {
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
    private lateinit var appBarLayout: AppBarLayout

    private val dictionaryViewModel: DictionaryViewModel by viewModels()
    private val entryViewModel: MyEntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_entry)

        alertDialogBuilder = MaterialAlertDialogBuilder(this)
        emptyCard = findViewById(R.id.empty_card)
        fetchProgress = findViewById(R.id.fetch_progress)
        searchView = findViewById(R.id.vocaby_search)
        appBarLayout = findViewById(R.id.app_layout)
        searchView.apply {
            setOnQueryChangeListener(queryChangeListener)
            setOnSearchListener(searchListener)
        }

        setupButtons()
        setupEntryBuilderDialog()
        setupEntryUpdateDialog()
        setupRecyclerView()

        lifecycleScope.launchWhenStarted {
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
                                else -> {}
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

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.custom_entry_container)
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
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0) {
                    entryViewModel.loadMoreEntries(true)
                }
            }
        })
    }

    private fun setupButtons() {
        val typeManagementButton = findViewById<Button>(R.id.type_management_button)
        typeManagementButton.setOnClickListener {
            val intent = Intent(this, TypeManagementActivity::class.java)
            startActivity(intent)
        }

        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        addFab = findViewById(R.id.add_entry_button)
        addFab.setOnClickListener { entryCreateDialog.show() }
    }

    private fun setupEntryBuilderDialog() {
        entryCreateDialog =
            BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
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
    }

    private fun updateEmptyCardVisibility() {
        if (customEntryAdapter.itemCount > 0) emptyCard.visibility = View.INVISIBLE
        else emptyCard.visibility = View.VISIBLE
    }

    private fun setupEntryUpdateDialog() {
        entryUpdateDialog =
            BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
        entryUpdateDialog.setContentView(R.layout.dialog_entry_item_action)

        entryEditButton = entryUpdateDialog.findViewById(R.id.edit_entry_button)!!
        entryDeleteButton = entryUpdateDialog.findViewById(R.id.delete_entry_button)!!

        entryUpdateDialog.setOnDismissListener {
            entryEditButton.setOnClickListener(null)
            entryDeleteButton.setOnClickListener(null)
        }
    }

    private val entryBuilderActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
        var startEntryBuilderIntent = Intent(this, EntryBuilderActivity::class.java)
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
        super.onDestroy()
    }
}