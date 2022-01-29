package com.vocaby.application.feature_save.presentation.collection

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.application.R
import com.vocaby.application.launchAndRepeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SaveCollectionFragment : Fragment(), SaveCollectionAdapter.Interaction {
    private val saveCollectionViewModel: SaveCollectionViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveCollectionAdapter: SaveCollectionAdapter
    private lateinit var collectionDialog: BottomSheetDialog
    private lateinit var addCollectionButton: Button
    private lateinit var collectionAlert: TextView
    private lateinit var collectionEdit: EditText
    private lateinit var dialogHeader: TextView
    private lateinit var dialogButton: Button
    private lateinit var collectionUpdateDialog: BottomSheetDialog
    private lateinit var collectionEditNameButton: Button
    private lateinit var collectionDeleteButton: Button
    private lateinit var allSaveCard: CardView
    private lateinit var allSaveHeader: TextView
    private lateinit var allSaveCount: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_collection, container, false)
        recyclerView = view.findViewById(R.id.save_collection_container)
        addCollectionButton = view.findViewById(R.id.add_collection_button)
        allSaveCard = view.findViewById(R.id.all_saves)
        allSaveHeader = view.findViewById(R.id.collection_header)
        allSaveCount = view.findViewById(R.id.collection_counter)

        setupCollectionDialog()
        setupRecyclerView()
        setupUpdateDialog()
        setupButtons()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            saveCollectionViewModel.uiEvent.collectLatest { event ->
                when (event) {
                    is CollectionItemsUiEvent.CloseCollectionDialog -> {
                        collectionDialog.dismiss()
                    }
                    is CollectionItemsUiEvent.ScrollToTop -> {
                        recyclerView.smoothScrollToPosition(0)
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
                }
            }
        }

        launchAndRepeatWithViewLifecycle {
            saveCollectionViewModel.allSaveCollectionState.collectLatest {
                allSaveHeader.text = it.collectionName
                val countText = "${it.count} Saved Entries"
                allSaveCount.text = countText
            }
        }

        launchAndRepeatWithViewLifecycle {
            saveCollectionViewModel.saveCollectionState.collectLatest {
                saveCollectionAdapter.submitList(it)
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

        allSaveCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_bottom_to_top,
                    R.anim.exit_top_to_bottom,
                    R.anim.enter_bottom_to_top,
                    R.anim.exit_top_to_bottom
                ).add(R.id.save_collection_fragment_container, SaveCollectionItemsFragment.newInstance("All Saves", true, id))
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireActivity().applicationContext, 2)
        recyclerView.setHasFixedSize(true)
        saveCollectionAdapter = SaveCollectionAdapter(this)
        recyclerView.adapter = saveCollectionAdapter
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

    override fun onItemTouch(name: String, id: Int) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
            ).add(R.id.save_collection_fragment_container, SaveCollectionItemsFragment.newInstance(name, false, id))
            .addToBackStack(null)
            .commit()
    }

    override fun onItemUpdate(collectionName: String, collectionId: Int) {
        saveCollectionViewModel.setCollection(collectionId, collectionName)
        collectionUpdateDialog.show()
    }
}