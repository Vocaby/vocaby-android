package com.vocaby.application.feature_save.presentation.collection

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.vocaby.application.R
import com.vocaby.application.core.util.GridItemDecoration
import com.vocaby.application.launchAndRepeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SaveCollectionFragment : Fragment(), SaveCollectionAdapter.Interaction {
    private val saveCollectionViewModel: SaveCollectionViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveCollectionAdapter: SaveCollectionAdapter
    private lateinit var dataObserver: RecyclerView.AdapterDataObserver
    private lateinit var addCollectionButton: ExtendedFloatingActionButton
    private lateinit var collectionDialog: BottomSheetDialog
    private lateinit var collectionAlert: TextView
    private lateinit var collectionEdit: EditText
    private lateinit var dialogHeader: TextView
    private lateinit var dialogButton: Button
    private lateinit var collectionUpdateDialog: BottomSheetDialog
    private lateinit var collectionEditNameButton: Button
    private lateinit var collectionDeleteButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_collection, container, false)
        recyclerView = view.findViewById(R.id.save_collection_container)
        addCollectionButton = view.findViewById(R.id.add_collection_button)

        setupRecyclerView()
        setupCollectionDialog()
        setupUpdateDialog()
        setupButtons()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            saveCollectionViewModel.saveCollectionState.collectLatest {
                saveCollectionAdapter.submitList(it)
            }
        }

        launchAndRepeatWithViewLifecycle {
            saveCollectionViewModel.uiEvent.collectLatest { event ->
                when (event) {
                    is CollectionItemsUiEvent.CloseCollectionDialog -> {
                        collectionDialog.dismiss()
                    }
                    is CollectionItemsUiEvent.ShowUpdateDialog -> {
                        collectionUpdateDialog.dismiss()
                        dialogHeader.setText(R.string.collection_update_name)
                        dialogButton.setText(R.string.update_collection)
                        dialogButton.setOnClickListener {
                            dialogButton.isEnabled = false
                            saveCollectionViewModel.updateCollection(collectionEdit.text.toString())
                        }
                        collectionEdit.setText(event.collectionName)
                        collectionDialog.show()
                    }
                    is CollectionItemsUiEvent.ShowCollectionAlert -> {
                        collectionAlert.visibility = View.VISIBLE
                        collectionAlert.text = event.message
                        dialogButton.isEnabled = true
                    }
                    is CollectionItemsUiEvent.ShowActionsDialog -> {
                        collectionUpdateDialog.show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupButtons() {
        addCollectionButton.setOnClickListener {
            dialogHeader.setText(R.string.add_a_save_collection)
            dialogButton.setText(R.string.create)
            dialogButton.setOnClickListener {
                dialogButton.isEnabled = false
                saveCollectionViewModel.addSaveCollection(collectionEdit.text.toString())
            }

            collectionDialog.show()
        }
    }

    private fun setupCollectionDialog() {
        collectionDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        collectionDialog.setContentView(R.layout.dialog_collection)
        collectionEdit = collectionDialog.findViewById(R.id.collection_name_input)!!
        collectionAlert = collectionDialog.findViewById(R.id.collection_header_alert)!!
        dialogHeader = collectionDialog.findViewById(R.id.dialog_header)!!
        dialogButton = collectionDialog.findViewById(R.id.dialog_collection_create_button)!!

        // Clear content on show
        collectionDialog.setOnDismissListener {
            collectionEdit.clearFocus()
            collectionEdit.text?.clear()
            collectionAlert.visibility = View.INVISIBLE
            dialogButton.isEnabled = true
        }

        val counter = collectionDialog.findViewById<TextView>(R.id.character_counter)!!
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                counter.text = s.length.toString()
            }
            override fun afterTextChanged(s: Editable) {}
        }

        collectionEdit.addTextChangedListener(textWatcher)
    }

    private fun setupUpdateDialog() {
        collectionUpdateDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        collectionUpdateDialog.setContentView(R.layout.dialog_collection_item_action)

        collectionEditNameButton = collectionUpdateDialog.findViewById(R.id.edit_collection_name_button)!!
        collectionDeleteButton = collectionUpdateDialog.findViewById(R.id.delete_collection_button)!!

        collectionEditNameButton.setOnClickListener {
            saveCollectionViewModel.prepareUpdateDialog()
        }

        collectionDeleteButton.setOnClickListener {
            saveCollectionViewModel.removeCollection()
            collectionUpdateDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        saveCollectionAdapter.registerAdapterDataObserver(dataObserver)
    }

    override fun onPause() {
        super.onPause()
        saveCollectionAdapter.unregisterAdapterDataObserver(dataObserver)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireActivity().applicationContext, 2)
        recyclerView.setHasFixedSize(true)
        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, requireActivity().applicationContext.resources.displayMetrics)
        recyclerView.addItemDecoration(GridItemDecoration(margin.toInt()))
        saveCollectionAdapter = SaveCollectionAdapter(this)
        dataObserver = object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.smoothScrollToPosition(0)
            }
        }
        recyclerView.adapter = saveCollectionAdapter
    }

    override fun onItemTouch(name: String, id: Int) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
            ).add(R.id.save_collection_fragment_container, SaveCollectionItemsFragment.newInstance(name, id))
            .addToBackStack(null)
            .commit()
    }

    override fun onItemUpdate(collectionName: String, collectionId: Int) {
        saveCollectionViewModel.setCollection(collectionId, collectionName)
    }
}