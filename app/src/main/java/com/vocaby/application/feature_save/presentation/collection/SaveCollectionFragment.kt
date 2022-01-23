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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.application.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SaveCollectionFragment : Fragment(), SaveCollectionAdapter.Interaction {
    private val saveCollectionViewModel: SaveCollectionViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveCollectionAdapter: SaveCollectionAdapter
    private lateinit var collectionCreateDialog: BottomSheetDialog
    private lateinit var addCollectionButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_collection, container, false)
        recyclerView = view.findViewById(R.id.save_collection_container)
        addCollectionButton = view.findViewById(R.id.add_collection_button)

        setupButtons()
        setupRecyclerView()
        setupCollectionCreateDialog()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            saveCollectionViewModel.saveCollectionState.collectLatest {
                saveCollectionAdapter.submitList(it)
            }
        }
    }

    private fun setupButtons() {
        addCollectionButton.setOnClickListener { collectionCreateDialog.show() }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireActivity().applicationContext, 2)
        saveCollectionAdapter = SaveCollectionAdapter(this)
        recyclerView.adapter = saveCollectionAdapter
    }

    private fun setupCollectionCreateDialog() {
        collectionCreateDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        collectionCreateDialog.setContentView(R.layout.collection_create_dialog)
        val collectionEdit: EditText = collectionCreateDialog.findViewById(R.id.collection_name_input)!!
        val collectionAlert: TextView = collectionCreateDialog.findViewById(R.id.collection_header_alert)!!
        val createButton = collectionCreateDialog.findViewById<Button>(R.id.dialog_collection_create_button)

        createButton?.setText(R.string.create)
        createButton?.setOnClickListener {
//            entryViewModel.createCustomEntry(entryEdit.text.toString())
        }

        // Clear content on show
        collectionCreateDialog.setOnShowListener {
            collectionEdit.clearFocus()
            collectionEdit.text?.clear()
            collectionAlert.visibility = View.INVISIBLE
        }

        val counter = collectionCreateDialog.findViewById<TextView>(R.id.character_counter)!!
        val textWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                counter.text = count.toString()
            }
            override fun afterTextChanged(s: Editable) {}
        }

        collectionEdit.addTextChangedListener(textWatcher)
    }

    override fun onItemTouch(name: String, allSaves: Boolean) {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom,
                R.anim.enter_bottom_to_top,
                R.anim.exit_top_to_bottom
            ).add(R.id.save_collection_fragment_container, SaveCollectionItemsFragment.newInstance(name, allSaves))
            .addToBackStack(null)
            .commit()
    }
}