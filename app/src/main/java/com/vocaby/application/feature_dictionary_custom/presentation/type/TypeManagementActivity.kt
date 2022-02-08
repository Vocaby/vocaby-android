package com.vocaby.application.feature_dictionary_custom.presentation.type

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.vocaby.application.R
import com.vocaby.application.core.util.DragStartListener
import com.vocaby.application.core.util.ItemTouchCallback
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.states.ItemState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TypeManagementActivity : AppCompatActivity(), DragStartListener, TypeAdapter.ItemInteractionListener{
    private lateinit var saveButton: Button
    private lateinit var addTypeButton: ExtendedFloatingActionButton
    private lateinit var createButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var typeAdapter: TypeAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var progressBar: ProgressBar
    private lateinit var typeCreationDialog: BottomSheetDialog
    private lateinit var typeNameEdit: EditText
    private lateinit var typeCreationAlert: TextView

    private val typeManagementViewModel: TypeManagementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_management)
        saveButton = findViewById(R.id.save_button)
        addTypeButton = findViewById(R.id.add_type_button)
        progressBar = findViewById(R.id.save_progress_bar)

        val closeButton = findViewById<Button>(R.id.back_button)
        closeButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        saveButton.setOnClickListener {
             saveButton.isEnabled = false
             progressBar.visibility = View.VISIBLE
            typeManagementViewModel.save()
        }

        setupRecyclerView()
        setupTypeCreationDialog()

        launchAndRepeatWithViewLifecycle {
            launch {
                typeManagementViewModel.typeState.collect {
                    typeAdapter.submitList(it)
                }
            }

            launch {
                typeManagementViewModel.uiEvent.collect { event ->
                    when(event) {
                        is TypeUiEvent.ShowAlert -> {
                            typeCreationAlert.text = event.message
                            typeCreationAlert.visibility = View.VISIBLE
                            createButton.isEnabled = true
                        }
                        is TypeUiEvent.UpdateAdapter -> {
                            when (event.state) {
                                ItemState.DELETE -> {
                                    typeAdapter.removeItem(event.position)
                                }
                                ItemState.ADD -> {
                                    typeAdapter.addItem()
                                    recyclerView.smoothScrollToPosition(0)
                                }
                                else -> {}
                            }

                            typeCreationDialog.dismiss()
                        }
                        is TypeUiEvent.CloseEditor -> {
                            if (event.hasChanges) setResult(RESULT_OK)
                            else setResult(RESULT_CANCELED)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.type_container)
        recyclerView.layoutManager = LinearLayoutManager(this)
        typeAdapter = TypeAdapter(this, this, this)
        recyclerView.adapter = typeAdapter

        val callback: ItemTouchHelper.Callback = ItemTouchCallback(typeAdapter)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun setupTypeCreationDialog() {
        typeCreationDialog =
            BottomSheetDialog(this, R.style.Theme_VocabyAndroid_BottomSheetDialog)
        typeCreationDialog.setContentView(R.layout.dialog_type_create)
        typeNameEdit = typeCreationDialog.findViewById(R.id.entry_edit)!!
        typeCreationAlert = typeCreationDialog.findViewById(R.id.entry_header_alert)!!
        createButton = typeCreationDialog.findViewById(R.id.dialog_entry_create_button)!!

        createButton.setText(R.string.create)
        createButton.setOnClickListener {
            createButton.isEnabled = false
            typeManagementViewModel.createType(typeNameEdit.text.toString())
        }

        typeCreationDialog.setOnShowListener {
            createButton.isEnabled = true
            typeNameEdit.text.clear()
            typeNameEdit.clearFocus()
            typeCreationAlert.visibility = View.INVISIBLE
        }

        addTypeButton.setOnClickListener {
            typeCreationDialog.show()
        }
    }

    override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onItemRemoved(position: Int) {
        val holder = recyclerView.findViewHolderForAdapterPosition(position)
        typeManagementViewModel.removeType(position)

        // Google's Implementation of ItemTouchHelper assumes that
        // the swiped items are cleaned up. Because the view is recycled
        // when swiped and not cleaned up with RecyclerView,
        // the view is positioned outside the recyclerview when a new item is added.
        // So we need to revert back the position by doing the following:
        if (holder != null) {
            holder.itemView.visibility = View.INVISIBLE
            typeAdapter.notifyItemChanged(position)
            itemTouchHelper.startSwipe(holder)
        }
    }
}