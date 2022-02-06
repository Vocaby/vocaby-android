package com.vocaby.application.feature_dictionary.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.vocaby.application.R
import com.vocaby.application.feature_save.domain.model.SaveModel
import com.vocaby.application.feature_save.domain.model.UpdateSaveCollectionModel
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.vocabywidgets.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchCollectionDialogFragment: DialogFragment() {
    private val searchCollectionDialogViewModel: SearchCollectionDialogViewModel by viewModels()
    private lateinit var saveCollectionButton: Button
    private lateinit var removeSaveButton: Button
    private lateinit var chipGroup: ChipGroup

    companion object {
        const val TAG = "CollectionDialogFragment"
        const val UPDATE_MODE = "UPDATE_MODE"
        const val SAVE_MODEL = "SAVE_MODEL"
        const val SAVE_COLLECTIONS = "SAVE_COLLECTIONS"
        const val REMOVE_ALL = "REMOVE_ALL"
        const val SHOW_SNACKBAR = "SHOW_DIALOG"

        @JvmStatic
        fun newInstance(
            updateMode:Boolean,
            saveModel: SaveModel?,
            collections: ArrayList<UpdateSaveCollectionModel>
        ): SearchCollectionDialogFragment {
            val fragment = SearchCollectionDialogFragment()
            val args = Bundle()
            args.putBoolean(UPDATE_MODE, updateMode)
            args.putParcelable(SAVE_MODEL, saveModel)
            args.putParcelableArrayList(SAVE_COLLECTIONS, collections)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_save_collection, container, false)
        chipGroup = view.findViewById(R.id.collection_chip_group)
        saveCollectionButton = view.findViewById(R.id.save_button)
        removeSaveButton = view.findViewById(R.id.remove_button)
        removeSaveButton.setOnClickListener {
            setFragmentResult(TAG, bundleOf(REMOVE_ALL to true))
            dismiss()
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                searchCollectionDialogViewModel.uiState.collect { state ->
                    when(state) {
                        is DialogUiState.UpdateUi -> {
                            if (state.updateMode) {
                                removeSaveButton.visibility = View.VISIBLE
                                saveCollectionButton.setText(R.string.collection_update)
                            }

                            saveCollectionButton.setOnClickListener {
                                searchCollectionDialogViewModel.updateItemInCollections()
                            }

                            for (collection in state.collections) {
                                val chip = layoutInflater.inflate(R.layout.chip_entry_group, chipGroup, false) as Chip
                                chip.text = collection.name
                                chip.isChecked = collection.saved
                                chip.setOnClickListener { collection.saved = !collection.saved }
                                chipGroup.addView(chip)
                            }
                        }
                        else -> {}
                    }
                }
            }

            launch {
                searchCollectionDialogViewModel.uiEvent.collect { event ->
                    when(event) {
                        is DialogUiEvent.CloseCollectionDialog -> {
                            setFragmentResult(TAG, bundleOf(
                                REMOVE_ALL to event.removeSave,
                                SHOW_SNACKBAR to event.showSnackBar
                            ))
                            dismiss()
                        }
                    }
                }
            }
        }

        return view
    }
}