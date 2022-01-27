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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.vocaby.application.R
import com.vocaby.application.states.UserInputState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SaveCollectionFragment : Fragment(), SaveCollectionAdapter.Interaction {
    private val saveCollectionViewModel: SaveCollectionViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var saveCollectionAdapter: SaveCollectionAdapter
    private lateinit var collectionCreateDialog: BottomSheetDialog
    private lateinit var addCollectionButton: Button
    private lateinit var collectionAlert: TextView
    private lateinit var collectionCreateButton: Button
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

        setupButtons()
        setupRecyclerView()
        setupCollectionCreateDialog()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            saveCollectionViewModel.allSaveCollectionState.collectLatest {
                allSaveHeader.text = it.name
                allSaveCount.text = "${it.count} Saved Entries"
            }
        }

        lifecycleScope.launchWhenStarted {
            saveCollectionViewModel.saveCollectionState.collectLatest {
                saveCollectionAdapter.submitList(it)
            }
        }

        lifecycleScope.launchWhenStarted {
            saveCollectionViewModel.createCollectionState.collectLatest { state ->
                when(state) {
                    is UserInputState.EmptyInput -> {
                        collectionAlert.setText(R.string.collection_name_empty)
                        collectionAlert.visibility = View.VISIBLE
                        collectionCreateButton.isEnabled = true
                    }
                    is UserInputState.LongInput -> {
                        collectionAlert.setText(R.string.collection_name_long)
                        collectionAlert.visibility = View.VISIBLE
                        collectionCreateButton.isEnabled = true
                    }
                    is UserInputState.SameInput -> {
                        collectionAlert.setText(R.string.collection_name_exists)
                        collectionAlert.visibility = View.VISIBLE
                        collectionCreateButton.isEnabled = true
                    }
                    else -> {
                        collectionAlert.visibility = View.INVISIBLE
                        collectionCreateDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        addCollectionButton.setOnClickListener { collectionCreateDialog.show() }

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

    private fun setupCollectionCreateDialog() {
        collectionCreateDialog =
            BottomSheetDialog(requireActivity(), R.style.Theme_VocabyAndroid_BottomSheetDialog)
        collectionCreateDialog.setContentView(R.layout.dialog_collection_create)
        val collectionEdit: EditText = collectionCreateDialog.findViewById(R.id.collection_name_input)!!
        collectionAlert = collectionCreateDialog.findViewById(R.id.collection_header_alert)!!
        collectionCreateButton = collectionCreateDialog.findViewById(R.id.dialog_collection_create_button)!!

        collectionCreateButton.setOnClickListener {
            collectionCreateButton.isEnabled = false
            saveCollectionViewModel.addSaveCollection(collectionEdit.text.toString())
        }

        // Clear content on show
        collectionCreateDialog.setOnShowListener {
            collectionEdit.clearFocus()
            collectionEdit.text?.clear()
            collectionAlert.visibility = View.INVISIBLE
            collectionCreateButton.isEnabled = true
        }

        val counter = collectionCreateDialog.findViewById<TextView>(R.id.character_counter)!!
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
}