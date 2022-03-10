package com.vocaby.application.feature_dictionary_custom.presentation.builder.group_builder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.vocaby.application.R
import com.vocaby.application.core.states.ItemState
import com.vocaby.application.core.util.DragStartListener
import com.vocaby.application.core.util.ItemTouchCallback
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
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
    private lateinit var appBar: AppBarLayout
    private lateinit var emptyCard: LinearLayout
    private lateinit var addDefinitionButton: ExtendedFloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_entry_group_builder)
        saveAlert = findViewById(R.id.definition_add_alert)
        val activityHeader = findViewById<TextView>(R.id.custom_group_activity_header)
        val typeHeader = findViewById<TextView>(R.id.type_header)
        emptyCard = findViewById(R.id.empty_card)
        appBar = findViewById(R.id.entry_app_bar)

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
                            updateEmptyCardVisibility()
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
                                    updateEmptyCardVisibility()
                                }
                                ItemState.UPDATE -> {
                                    customDefAdapter.updateItem(event.position)
                                }
                                ItemState.DELETE -> {
                                    customDefAdapter.notifyItemRemoved(event.position)
                                    updateEmptyCardVisibility()
                                }
                                else -> {}
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

    private fun updateEmptyCardVisibility() {
        if (customDefAdapter.itemCount == 0) {
            val show = AlphaAnimation(0f, 1.0f)
            show.duration = 300

            emptyCard.visibility = View.VISIBLE
            emptyCard.startAnimation(show)
        } else {
            emptyCard.visibility = View.INVISIBLE
        }
    }

    private fun setupDefinitionBuilder() {
        definitionBuilderDialog = BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog).apply {
            setContentView(R.layout.dialog_custom_entry_definition_builder)
        }
        definitionBuilderDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialogDefinitionInput = definitionBuilderDialog.findViewById(R.id.definition_edit)!!
        dialogExampleInput = definitionBuilderDialog.findViewById(R.id.example_edit)!!
        definitionAlertView = definitionBuilderDialog.findViewById(R.id.definition_header_alert)!!
        dialogHeader = definitionBuilderDialog.findViewById(R.id.definition_dialog_header)!!

        definitionBuilderDialog.setOnShowListener {
            definitionAlertView.text = ""
            dialogDefinitionInput.clearFocus()
            dialogExampleInput.clearFocus()
        }

        definitionBuilderDialog.findViewById<LinearLayout>(R.id.dialog_container)?.setOnClickListener {
            definitionBuilderDialog.currentFocus?.let { view ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
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
        addDefinitionButton = findViewById(R.id.add_definition_button)
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
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(-1)
                    && newState==RecyclerView.SCROLL_STATE_IDLE
                    && addDefinitionButton.translationY > 100f) {
                    val show = AlphaAnimation(0f, 1.0f)
                    show.duration = 300
                    addDefinitionButton.startAnimation(show)
                    addDefinitionButton.translationY = 0f
                }
            }
        })

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

    override fun onDestroy() {
        recyclerView.clearOnScrollListeners()
        super.onDestroy()
    }
}