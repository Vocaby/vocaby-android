package com.vocaby.application.feature_dictionary_custom.presentation.ui

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
import com.vocaby.application.core.util.LiveDataUtil.observeOnce
import com.vocaby.application.feature_dictionary_custom.presentation.adapter.CustomGroupAdapter
import com.vocaby.application.feature_dictionary_custom.presentation.adapter.TypeAdapter
import com.vocaby.application.feature_dictionary_custom.presentation.viewmodel.EntryViewModel
import com.vocaby.application.payloads.ItemIntPayload
import com.vocaby.application.states.ItemState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryBuilderActivity : AppCompatActivity(), DragStartListener,
    CustomGroupAdapter.ItemInteractionListener, TypeAdapter.ItemInteractionListener {
    private lateinit var editorHeader: TextView
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var customGroupAdapter: CustomGroupAdapter
    private lateinit var typeAdapter: TypeAdapter
    private lateinit var saveProgressBar: ProgressBar
    private lateinit var groupAlert: TextView
    private lateinit var pronunciationInput: EditText
    private lateinit var groupBuilder: BottomSheetDialog
    private lateinit var helpDialogBuilder: BottomSheetDialog
    private lateinit var createGroupButton: Button
    private lateinit var typeCreatorAlert: TextView

    private val entryViewModel: EntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_entry_builder)
        val entryView = findViewById<TextView>(R.id.entry_header)

        if (savedInstanceState != null) {
            entryViewModel.repopulateUI()
        }

        editorHeader = findViewById(R.id.editor_header)

        groupAlert = findViewById(R.id.group_header_alert)
        pronunciationInput = findViewById(R.id.feedback_input)
        setUpGroupBuilder()
        setupRecyclerView()
        setupButtons()

        entryViewModel.editorState.observeOnce(this) { itemState ->
            when (itemState) {
                ItemState.ADD -> editorHeader.text = getString(R.string.add_dictionary_entry)
                else -> {
                    editorHeader.text = getString(R.string.update_dictionary_entry)
                }
            }
        }

        entryViewModel.types.observe(this) { newList ->
            typeAdapter.setList(newList)
        }

        entryViewModel.definitionGroups.observeOnce(this) { list ->
            customGroupAdapter.setList(list)
        }

        entryViewModel.pronunciation.observeOnce(this) { pronunciation ->
            pronunciationInput.setText(
                pronunciation,
                TextView.BufferType.EDITABLE
            )
        }

        entryViewModel.groupChange.observe(this) { groupPayload: ItemIntPayload ->
            when (groupPayload.state) {
                ItemState.ADD -> {
                    groupAlert.visibility = View.INVISIBLE
                    customGroupAdapter.addItem()
                }
                ItemState.DELETE -> {
                    customGroupAdapter.removeItem(groupPayload.payload)
                }
                ItemState.UPDATE -> {
                    customGroupAdapter.editItem(groupPayload.payload)
                }
            }
        }

        entryViewModel.typeChange.observe(this) { typePayload ->
            when (typePayload.state) {
                ItemState.ADD -> {
                    typeAdapter.addItem(typePayload.payload)
                }
                ItemState.DELETE -> {
                    typeAdapter.removeItem(typePayload.payload)
                }
                else -> {}
            }
        }

        entryViewModel.saveResult.observeOnce(this) { saveSuccessful ->
            if (saveSuccessful) {
                setResult(RESULT_OK, entryViewModel.addEntryResultDataToIntent())
            } else {
                setResult(RESULT_CANCELED)
            }

            finish()
        }

        entryViewModel.selectedType.observe(this) { type ->
            createGroupButton.setOnClickListener {
                if (type.isEmpty()) {
                    typeCreatorAlert.visibility = View.VISIBLE
                } else {
                    typeCreatorAlert.visibility = View.INVISIBLE

                    var groupBuilderActivityData =
                        Intent(this, EntryGroupBuilderActivity::class.java)
                    groupBuilderActivityData =
                        entryViewModel.addNewGroupDataToIntent(groupBuilderActivityData, type)
                    groupBuilderActivity.launch(groupBuilderActivityData)
                    groupBuilder.dismiss()
                }
            }
        }

        entryViewModel.entry.observeOnce(this) { text -> entryView.text = text }
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
            entryViewModel.saveUserEntry(pronunciationInput.text.toString())
        }

        // Add new group button
        val addGroupButton = findViewById<Button>(R.id.add_def_group_button)
        addGroupButton.setOnClickListener { groupBuilder.show() }
        createGroupButton = groupBuilder.findViewById(R.id.create_group_button)!!
        typeCreatorAlert = groupBuilder.findViewById(R.id.type_creator_alert)!!

        val helpButton = findViewById<TextView>(R.id.help_button)
        helpButton.setOnClickListener {
            helpDialogBuilder.show()
        }
    }

    private fun setUpGroupBuilder() {
        helpDialogBuilder = BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
        helpDialogBuilder.setContentView(R.layout.card_instruction)

        groupBuilder = BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
        groupBuilder.setContentView(R.layout.custom_entry_group_builder_dialog)
        groupBuilder.setOnShowListener { groupAlert.visibility = View.INVISIBLE }

        val builderRecyclerView = groupBuilder.findViewById<RecyclerView>(R.id.type_container)!!
        builderRecyclerView.setHasFixedSize(true)
        builderRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        typeAdapter = TypeAdapter(this)
        builderRecyclerView.adapter = typeAdapter
    }

    private val groupBuilderActivity = registerForActivityResult(StartActivityForResult()) {
            result: ActivityResult -> entryViewModel.handleGroupCreationResult(result)
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onGroupCardClicked(position: Int) {
        entryViewModel.setSelectedGroup(position)
        var groupBuilderActivityData = Intent(this, EntryGroupBuilderActivity::class.java)
        groupBuilderActivityData =
            entryViewModel.addExistingGroupDataToIntent(groupBuilderActivityData, position)

        groupBuilderActivity.launch(groupBuilderActivityData)
    }

    override fun onItemRemoved(position: Int) {
        val holder = recyclerView.findViewHolderForAdapterPosition(position)
        entryViewModel.removeGroup(ItemState.UPDATE, position)

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

    override fun onTypeClicked(type: String) {
        entryViewModel.setSelectedType(type)
    }
}