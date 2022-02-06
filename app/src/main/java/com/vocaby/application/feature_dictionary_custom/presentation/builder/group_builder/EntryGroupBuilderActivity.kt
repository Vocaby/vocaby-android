package com.vocaby.application.feature_dictionary_custom.presentation.builder.group_builder

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.application.R
import com.vocaby.application.core.util.DragStartListener
import com.vocaby.application.core.util.ItemTouchCallback
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.states.ItemState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EntryGroupBuilderActivity : AppCompatActivity(), DragStartListener,
    CustomDefAdapter.ItemInteractionListener {
    private val entryGroupViewModel: EntryGroupViewModel by viewModels()

    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var definitionBuilderDialog: BottomSheetDialog
    private lateinit var definitionAlertView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var customDefAdapter: CustomDefAdapter
    private lateinit var saveAlert: TextView
    private lateinit var dialogDefinitionInput: EditText
    private lateinit var dialogExampleInput: EditText
    private lateinit var dialogHeader: TextView
    private lateinit var dialogSaveButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_entry_group_builder)
        saveAlert = findViewById(R.id.definition_add_alert)
        val activityHeader = findViewById<TextView>(R.id.custom_group_activity_header)
        val typeHeader = findViewById<TextView>(R.id.type_header)


        setupDefinitionBuilder()
        setupButtons()
        setupRecyclerView()
        launchAndRepeatWithViewLifecycle {
            launch {
                entryGroupViewModel.uiState.collect { state ->
                    when(state) {
                        is EntryGroupBuilderUiState.UpdateUiState -> {
                            customDefAdapter.setList(state.definitions)
                            activityHeader.text = state.typeHeader
                            typeHeader.text = state.type
                        }
                        is EntryGroupBuilderUiState.InProgress -> {}
                    }
                }
            }

            launch {
                entryGroupViewModel.uiEvent.collect { event ->
                    when(event) {
                        is EntryGroupBuilderUiEvent.UpdateAdapter -> {
                            when (event.state) {
                                ItemState.ADD -> {
                                    customDefAdapter.addItem()
                                }
                                ItemState.UPDATE -> {
                                    customDefAdapter.updateItem(event.position)
                                }
                                ItemState.DELETE -> {
                                    entryGroupViewModel.removeDefinition(event.position)
                                    customDefAdapter.notifyItemRemoved(event.position)
                                }
                            }

                            definitionBuilderDialog.dismiss()
                        }
                        is EntryGroupBuilderUiEvent.CloseBuilder -> {
                            setResult(RESULT_OK, event.resultData)
                            finish()
                        }
                        is EntryGroupBuilderUiEvent.ShowAlert -> {
                            definitionAlertView.text = event.message
                        }
                        is EntryGroupBuilderUiEvent.CloseDialog -> {
                            if (definitionBuilderDialog.isShowing) definitionBuilderDialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun setupDefinitionBuilder() {
        definitionBuilderDialog = BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
        definitionBuilderDialog.setContentView(R.layout.dialog_custom_entry_definition_builder)

        dialogDefinitionInput = definitionBuilderDialog.findViewById(R.id.definition_edit)!!
        dialogExampleInput = definitionBuilderDialog.findViewById(R.id.example_edit)!!
        definitionAlertView = definitionBuilderDialog.findViewById(R.id.definition_header_alert)!!
        dialogHeader = definitionBuilderDialog.findViewById(R.id.definition_dialog_header)!!

        definitionBuilderDialog.setOnShowListener {
            definitionAlertView.text = ""
            dialogDefinitionInput.clearFocus()
            dialogExampleInput.clearFocus()
        }

        // Add New Definition
        dialogSaveButton = definitionBuilderDialog.findViewById(R.id.save_definition_button)!!
    }

    private fun setupButtons() {
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Add Definition Button
        val addDefinitionButton = findViewById<Button>(R.id.add_definition_button)
        addDefinitionButton.setOnClickListener {
            dialogDefinitionInput.text.clear()
            dialogExampleInput.text.clear()
            dialogHeader.setText(R.string.create_a_definition)
            dialogSaveButton.setText(R.string.add_definition_button)

            dialogSaveButton.setOnClickListener {
                val definition = dialogDefinitionInput.text.toString()
                val example = dialogExampleInput.text.toString()
                entryGroupViewModel.addDefinition(definition, example)
            }

            definitionBuilderDialog.show()
        }

        // Save Button
        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener { button ->
            saveAlert.visibility = View.INVISIBLE
            button.isEnabled = false
            entryGroupViewModel.saveEntryGroup()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.custom_entry_definition_container)
        recyclerView.layoutManager = LinearLayoutManager(this)

        customDefAdapter = CustomDefAdapter(this, this, this)
        recyclerView.adapter = customDefAdapter

        itemTouchHelper = ItemTouchHelper(ItemTouchCallback(customDefAdapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onItemRemoved(position: Int) {
        val holder = recyclerView.findViewHolderForAdapterPosition(position)
        entryGroupViewModel.removeDefinition(position)
        customDefAdapter.notifyItemRemoved(position)

        // Google's Implementation of ItemTouchHelper assumes that
        // the swiped items are cleaned up. Because the view is recycled
        // when swiped and not cleaned up with RecyclerView,
        // the view is positioned outside the recyclerview when a new item is added.
        // So we need to revert back the position by doing the following:
        if (holder != null) {
            holder.itemView.visibility = View.INVISIBLE
            customDefAdapter.notifyItemChanged(position)
            itemTouchHelper.startSwipe(holder)
        }

        // Alternatively, I could remove the view from the layout manager
        // by simply doing recyclerView.removeViewAt(position)
        // but this would not make use of recycling.
    }

    override fun onItemTouched(position: Int, definition: String, example: String) {
        dialogHeader.setText(R.string.update_a_definition)
        dialogSaveButton.setText(R.string.update_definition_button)
        dialogDefinitionInput.setText(definition)
        dialogExampleInput.setText(example)

        dialogSaveButton.setOnClickListener {
            val newDefinition = dialogDefinitionInput.text.toString()
            val newExample = dialogExampleInput.text.toString()
            entryGroupViewModel.updateDefinition(position, definition, example, newDefinition, newExample)
        }

        definitionBuilderDialog.show()
    }
}