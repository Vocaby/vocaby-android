package com.vocaby.app.ui.customentry

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.app.R
import com.vocaby.app.adapters.CustomDefAdapter
import com.vocaby.app.adapters.DragStartListener
import com.vocaby.app.adapters.ItemTouchCallback
import com.vocaby.app.utils.LiveDataUtil.observeOnce
import com.vocaby.app.utils.StringFormatter
import com.vocaby.app.viewmodels.EntryGroupViewModel

class EntryGroupBuilderActivity : AppCompatActivity(), DragStartListener,
    CustomDefAdapter.ItemInteractionListener {
    private val entryGroupViewModel: EntryGroupViewModel by viewModels()

    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var definitionBuilder: BottomSheetDialog
    private lateinit var definitionAlertView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var customDefAdapter: CustomDefAdapter
    private lateinit var saveAlert: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_entry_group_builder_activity)
        saveAlert = findViewById(R.id.definition_add_alert)

        setupDefinitionBuilder()
        setupButtons()
        setupRecyclerView()

        entryGroupViewModel.handleIntent(intent)
        entryGroupViewModel.definitions.observe(this) { list -> customDefAdapter.setList(list) }

        entryGroupViewModel.type.observeOnce(this) { type ->
            val header = StringFormatter.firstLetterUpperOnly(type) + " Definitions"
            val activityHeader = findViewById<TextView>(R.id.custom_group_activity_header)
            val typeHeader = findViewById<TextView>(R.id.type_header)

            activityHeader.text = header
            typeHeader.text = type
        }

        entryGroupViewModel.definitionAlert.observe(this) { alertState ->
            definitionAlertView.text = getString(alertState.text)
            definitionAlertView.visibility = alertState.visibility
        }

        entryGroupViewModel.definitionAddStatus.observe(this) { added ->
            if (added) {
                customDefAdapter.addItem()
                definitionBuilder.dismiss()
            }
        }
    }

    private fun setupDefinitionBuilder() {
        definitionBuilder = BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
        definitionBuilder.setContentView(R.layout.custom_entry_definition_builder_dialog)

        val definitionView = definitionBuilder.findViewById<EditText>(R.id.definition_edit)!!
        val exampleView = definitionBuilder.findViewById<EditText>(R.id.example_edit)!!
        val closeButton = definitionBuilder.findViewById<Button>(R.id.close_button)!!
        definitionAlertView = definitionBuilder.findViewById(R.id.definition_header_alert)!!

        // Close Definition Builder Button
        closeButton.setOnClickListener {
            entryGroupViewModel.removeAlert()
            definitionBuilder.dismiss()
        }

        definitionBuilder.setOnShowListener {
            definitionView.text.clear()
            definitionView.clearFocus()
            exampleView.text.clear()
            exampleView.clearFocus()
        }

        // Add New Definition
        val addDefinitionButton = definitionBuilder.findViewById<Button>(R.id.create_definition_button)!!
        addDefinitionButton.setOnClickListener {
            val definition = definitionView.text.toString()
            val example = exampleView.text.toString()
            entryGroupViewModel.addDefinition(definition, example)
        }
    }

    private fun setupButtons() {
        val backButton = findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        // Add Definition Button
        val addDefinitionButton = findViewById<Button>(R.id.add_definition_button)
        addDefinitionButton.setOnClickListener { definitionBuilder.show() }

        // Save Button
        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            saveAlert.visibility = View.INVISIBLE
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
}