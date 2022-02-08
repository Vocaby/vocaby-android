package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.application.R
import com.vocaby.application.core.util.DragStartListener
import com.vocaby.application.core.util.ItemTouchCallback
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.feature_dictionary.presentation.search.SearchCollectionDialogFragment
import com.vocaby.application.feature_dictionary_custom.presentation.builder.group_builder.EntryGroupBuilderActivity
import com.vocaby.application.states.ItemState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EntryBuilderActivity : AppCompatActivity(), DragStartListener,
    CustomGroupAdapter.ItemInteractionListener {
    private lateinit var entryView: TextView
    private lateinit var editorHeader: TextView
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var customGroupAdapter: CustomGroupAdapter
    private lateinit var saveProgressBar: ProgressBar
    private lateinit var groupAlert: TextView
    private lateinit var pronunciationInput: EditText
    private lateinit var helpDialogBuilder: BottomSheetDialog

    private val entryBuilderViewModel: EntryBuilderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_entry_builder)
        entryView = findViewById(R.id.entry_header)
        editorHeader = findViewById(R.id.editor_header)
        groupAlert = findViewById(R.id.group_header_alert)
        pronunciationInput = findViewById(R.id.feedback_input)


        setUpBottomSheet()
        setupRecyclerView()
        setupButtons()
        setupFragmentManager()

        launchAndRepeatWithViewLifecycle {
            launch {
                collectUiState()
            }
            launch {
                collectUiEvent()
            }
        }
    }
    private suspend fun collectUiState() {
        entryBuilderViewModel.uiState.collect { state ->
            when (state) {
                is EntryBuilderUiState.InProgress -> {}
                is EntryBuilderUiState.UpdateUiState -> {
                    editorHeader.text = state.header
                    entryView.text = state.entry
                    pronunciationInput.setText(state.pronunciation, TextView.BufferType.EDITABLE)
                    customGroupAdapter.setList(state.groups)
                }
            }

        }
    }

    private suspend fun collectUiEvent() {
        entryBuilderViewModel.uiEvent.collect { event ->
            when(event) {
                is EntryBuilderUiEvent.UpdateAdapter -> {
                    when (event.state) {
                        ItemState.ADD -> {
                            groupAlert.visibility = View.INVISIBLE
                            customGroupAdapter.addItem()
                        }
                        ItemState.DELETE -> {
                            customGroupAdapter.removeItem(event.position)
                        }
                        ItemState.UPDATE -> {
                            customGroupAdapter.editItem(event.position)
                        }
                    }
                }
                is EntryBuilderUiEvent.ShowTypeSelectionDialog -> {
                    val dialogFragment = EntryBuilderGroupDialogFragment.newInstance(event.types)
                    dialogFragment.show(supportFragmentManager, SearchCollectionDialogFragment.TAG)
                }
                is EntryBuilderUiEvent.CloseBuilder -> {
                    setResult(RESULT_OK, event.resultData)
                    finish()
                }
                is EntryBuilderUiEvent.CancelBuilder -> {
                    setResult(RESULT_CANCELED)
                    finish()
                }
                is EntryBuilderUiEvent.OpenGroupBuilder -> {
                    var groupBuilderActivityData = Intent(this, EntryGroupBuilderActivity::class.java)
                    groupBuilderActivityData =
                        entryBuilderViewModel.addNewGroupDataToIntent(groupBuilderActivityData, event.selectedType)
                    groupBuilderActivity.launch(groupBuilderActivityData)
                }
            }
        }
    }

    private fun setupFragmentManager() {
        supportFragmentManager.setFragmentResultListener(
            EntryBuilderGroupDialogFragment.TAG,
            this
        ) { _, bundle -> entryBuilderViewModel.handleDialogResult(bundle) }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.custom_entry_group_container)
        recyclerView.layoutManager = LinearLayoutManager(this)
        customGroupAdapter = CustomGroupAdapter(this, this, this)
        recyclerView.adapter = customGroupAdapter

        val callback: ItemTouchHelper.Callback = ItemTouchCallback(customGroupAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun setupButtons() {
        // Close Entry Builder Button
        val closeButton = findViewById<Button>(R.id.back_button)
        closeButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Save Custom Entry Button
        val saveButton = findViewById<Button>(R.id.save_button)
        saveProgressBar = findViewById(R.id.save_progress_bar)
        saveButton.setOnClickListener { button ->
            saveProgressBar.visibility = View.VISIBLE
            button.isEnabled = false
            entryBuilderViewModel.saveUserEntry(pronunciationInput.text.toString())
        }

        // Add new group button
        val addGroupButton = findViewById<Button>(R.id.add_def_group_button)
        addGroupButton.setOnClickListener { entryBuilderViewModel.getAvailableTypes() }

        val helpButton = findViewById<TextView>(R.id.help_button)
        helpButton.setOnClickListener {
            helpDialogBuilder.show()
        }
    }

    private fun setUpBottomSheet() {
        helpDialogBuilder = BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
        helpDialogBuilder.setContentView(R.layout.card_instruction)
    }

    private val groupBuilderActivity = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult -> entryBuilderViewModel.handleGroupCreationResult(result)
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onGroupCardClicked(position: Int) {
        entryBuilderViewModel.setSelectedGroup(position)
        var groupBuilderActivityData = Intent(this, EntryGroupBuilderActivity::class.java)
        groupBuilderActivityData =
            entryBuilderViewModel.addExistingGroupDataToIntent(groupBuilderActivityData, position)

        groupBuilderActivity.launch(groupBuilderActivityData)
    }

    override fun onItemRemoved(position: Int) {
        val holder = recyclerView.findViewHolderForAdapterPosition(position)
        entryBuilderViewModel.removeGroup(ItemState.UPDATE, position)

        // Google's Implementation of ItemTouchHelper assumes that
        // the swiped items are cleaned up. Because the view is recycled
        // when swiped and not cleaned up with RecyclerView,
        // the view is positioned outside the recyclerview when a new item is added.
        // So we need to revert back the position by doing the following:
        if (holder != null) {
            holder.itemView.visibility = View.INVISIBLE
            customGroupAdapter.notifyItemChanged(position)
            itemTouchHelper.startSwipe(holder)
        }
    }
}