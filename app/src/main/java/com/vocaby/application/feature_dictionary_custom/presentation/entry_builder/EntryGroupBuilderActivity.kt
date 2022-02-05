package com.vocaby.application.feature_dictionary_custom.presentation.entry_builder

import android.content.Intent
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
import com.vocaby.application.core.util.Formatter
import com.vocaby.application.core.util.ItemTouchCallback
import com.vocaby.application.core.util.LiveDataUtil.observeOnce
import com.vocaby.application.states.ItemState
import com.vocaby.application.states.UserInputState

class EntryGroupBuilderActivity : AppCompatActivity(), DragStartListener,
    CustomDefAdapter.ItemInteractionListener {
    private val entryGroupViewModel: EntryGroupViewModel by viewModels()

    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var definitionBuilder: BottomSheetDialog
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

        setupDefinitionBuilder()
        setupButtons()
        setupRecyclerView()

        entryGroupViewModel.handleIntent(intent)

        entryGroupViewModel.definitions.observeOnce(this) { list ->
            customDefAdapter.setList(list)
        }

        entryGroupViewModel.type.observeOnce(this) { type ->
            val header = Formatter.firstLetterUpperOnly(type) + " Group"
            val activityHeader = findViewById<TextView>(R.id.custom_group_activity_header)
            val typeHeader = findViewById<TextView>(R.id.type_header)

            activityHeader.text = header
            typeHeader.text = type
        }

        entryGroupViewModel.inputState.observe(this) { input ->
            when (input) {
                is UserInputState.EmptyInput -> {
                    definitionAlertView.text = getString(R.string.definition_empty_alert)
                }
                is UserInputState.InvalidInput -> {
                    definitionAlertView.text = getString(R.string.definition_exists_alert)
                }
                is UserInputState.SameInput<*> -> {
                    definitionAlertView.text = getString(R.string.no_changes)
                }
                is UserInputState.Valid<*> -> {
                    definitionAlertView.text = input.data.toString()
                }
                else -> {
                    definitionAlertView.text = ""
                }
            }
        }

        entryGroupViewModel.definitionState.observe(this) { payload ->
            if (payload.state == ItemState.ADD) {
                customDefAdapter.addItem()
            } else if (payload.state == ItemState.UPDATE) {
                customDefAdapter.updateItem(payload.payload)
            } else if (payload.state == ItemState.DELETE) {
                entryGroupViewModel.removeDefinition(payload.payload)
                customDefAdapter.notifyItemRemoved(payload.payload)
            }

            definitionBuilder.dismiss()
        }
    }

    private fun setupDefinitionBuilder() {
        definitionBuilder = BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
        definitionBuilder.setContentView(R.layout.dialog_custom_entry_definition_builder)

        dialogDefinitionInput = definitionBuilder.findViewById(R.id.definition_edit)!!
        dialogExampleInput = definitionBuilder.findViewById(R.id.example_edit)!!
        definitionAlertView = definitionBuilder.findViewById(R.id.definition_header_alert)!!
        dialogHeader = definitionBuilder.findViewById(R.id.definition_dialog_header)!!

        definitionBuilder.setOnShowListener {
            definitionAlertView.text = ""
            dialogDefinitionInput.clearFocus()
            dialogExampleInput.clearFocus()
        }

        // Add New Definition
        dialogSaveButton = definitionBuilder.findViewById(R.id.save_definition_button)!!
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

            definitionBuilder.show()
        }

        // Save Button
        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener { button ->
            saveAlert.visibility = View.INVISIBLE
            button.isEnabled = false
            var saveIntent = Intent()
            saveIntent = entryGroupViewModel.addSaveDataToIntent(saveIntent)
            setResult(RESULT_OK, saveIntent)
            finish()
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

        definitionBuilder.show()
    }
}