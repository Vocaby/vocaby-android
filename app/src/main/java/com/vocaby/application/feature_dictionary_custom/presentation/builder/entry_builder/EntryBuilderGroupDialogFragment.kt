package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.vocaby.application.R
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.vocabywidgets.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EntryBuilderGroupDialogFragment: DialogFragment() {
    private val viewModel: EntryBuilderDialogViewModel by viewModels()
    private lateinit var groupAlert: TextView
    private lateinit var addGroupButton: Button
    private lateinit var chipGroup: ChipGroup

    companion object {
        const val TAG = "EntryBuilderGroupDialogFragment"
        const val AVAILABLE_TYPES = "AVAILABLE_TYPES"
        const val SELECTED_TYPE = "SELECTED_TYPE"

        @JvmStatic
        fun newInstance(
            types: ArrayList<String>
        ): EntryBuilderGroupDialogFragment {
            val fragment = EntryBuilderGroupDialogFragment()
            val args = Bundle()
            args.putStringArrayList(AVAILABLE_TYPES, types)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_entry_type_picker, container, false)
        chipGroup = view.findViewById(R.id.types_chip_group)
        addGroupButton = view.findViewById(R.id.create_group_button)
        groupAlert = view.findViewById(R.id.type_creator_alert)

        addGroupButton.setOnClickListener {
            viewModel.validate(chipGroup.checkedChipId)
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DialogUiState.UpdateUi -> {
                            for (type in state.types) {
                                val chip = layoutInflater.inflate(
                                    R.layout.chip_type,
                                    chipGroup,
                                    false
                                ) as Chip

                                chip.text = type
                                chipGroup.addView(chip)
                            }
                        }
                        is DialogUiState.ShowAlert -> {
                            groupAlert.visibility = View.VISIBLE
                        }
                        else -> {}
                    }
                }
            }

            launch {
                viewModel.uiEvent.collect { event ->
                    when(event) {
                        is DialogUiEvent.ShowAlert -> {
                            groupAlert.visibility = View.VISIBLE
                        }
                        is DialogUiEvent.CloseDialog -> {
                            val selectedType = chipGroup.findViewById<Chip>(chipGroup.checkedChipId).text
                            setFragmentResult(TAG, bundleOf(SELECTED_TYPE to selectedType))
                            dismiss()
                        }
                    }
                }
            }
        }

        return view
    }
}