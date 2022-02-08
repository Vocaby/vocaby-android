package com.vocaby.application.feature_dictionary_custom.presentation.builder.entry_builder

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.vocaby.application.R
import com.vocaby.application.feature_dictionary.data.local.entity.Type
import com.vocaby.application.feature_dictionary_custom.presentation.type.TypeManagementActivity
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
    private lateinit var typeManagementButton: ImageButton

    companion object {
        const val TAG = "EntryBuilderGroupDialogFragment"
        const val AVAILABLE_TYPES = "AVAILABLE_TYPES"
        const val SELECTED_TYPE = "SELECTED_TYPE"
        const val TYPES_CHANGED = "TYPES_CHANGED"

        @JvmStatic
        fun newInstance(
            types: ArrayList<Type>
        ): EntryBuilderGroupDialogFragment {
            val fragment = EntryBuilderGroupDialogFragment()
            val args = Bundle()
            args.putParcelableArrayList(AVAILABLE_TYPES, types)
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
        typeManagementButton = view.findViewById(R.id.type_management_button)

        addGroupButton.setOnClickListener {
            viewModel.validate(chipGroup.checkedChipId)
        }

        typeManagementButton.setOnClickListener {
            val intent = Intent(requireActivity(), TypeManagementActivity::class.java)
            typeManagementActivity.launch(intent)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DialogUiState.UpdateUi -> {
                            chipGroup.removeAllViews()
                            for (typeModel in state.types) {
                                val chip = layoutInflater.inflate(
                                    R.layout.chip_type,
                                    chipGroup,
                                    false
                                ) as Chip

                                chip.text = typeModel.type
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
                            val bundle = Bundle()
                            if (event.selected) {
                                val selectedType = chipGroup.findViewById<Chip>(chipGroup.checkedChipId).text.toString()
                                bundle.putString(SELECTED_TYPE, selectedType)
                            }

                            bundle.putBoolean(TYPES_CHANGED, event.typesChanged)

                            setFragmentResult(TAG, bundle)
                            dismiss()
                        }
                    }
                }
            }
        }

        return view
    }

    private val typeManagementActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult -> viewModel.handleResult(result)
    }
}