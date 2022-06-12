package com.vocaby.application.feature_profile.presentation.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.vocaby.application.R
import com.vocaby.application.feature_profile.domain.model.NotificationSettings
import com.vocaby.vocabywidgets.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationSettingsDialogFragment: DialogFragment() {
    private val viewModel: NotificationSettingsDialogViewModel by viewModels()
    private lateinit var header: TextView
    private lateinit var groupAlert: TextView
    private lateinit var addGroupButton: Button
    private lateinit var chipGroup: ChipGroup

    companion object {
        const val TAG = "NotificationSettingsDialogFragment"
        const val TITLE = "NotificationSettingsDialogTitle"
        const val TYPE = "NotificationSettingsDialogType"
        const val SELECTEDINDEX = "NotificationSettingsDialogSelected"
        const val NOTIFICATION_LIST = "NOTIFICATION_LIST"
        const val SELECTED_POSITION = "SELECTED_POSITION"

        @JvmStatic
        fun newInstance(
            title: String,
            list: ArrayList<String>,
            selected: Int,
            type: NotificationSettings,
        ): NotificationSettingsDialogFragment {
            val fragment = NotificationSettingsDialogFragment()
            val args = Bundle()
            args.putString(TITLE, title)
            args.putStringArrayList(NOTIFICATION_LIST, list)
            args.putSerializable(TYPE, type)
            args.putInt(SELECTEDINDEX, selected)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_fragment_notification_settings, container, false)
        header = view.findViewById(R.id.notification_dialog_header)
        chipGroup = view.findViewById(R.id.types_chip_group)
        addGroupButton = view.findViewById(R.id.update_settings_button)
        groupAlert = view.findViewById(R.id.type_creator_alert)

        addGroupButton.setOnClickListener {
            viewModel.validate(chipGroup.checkedChipId, chipGroup)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            launch {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DialogUiState.UpdateUi -> {
                            header.text = state.header

                            chipGroup.removeAllViews()
                            for ((index, item) in state.items.withIndex()) {
                                val chip = layoutInflater.inflate(
                                    R.layout.chip_notification,
                                    chipGroup,
                                    false
                                ) as Chip

                                chip.text = item
                                if (index == state.selectedIndex) chip.isChecked = true
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
                            bundle.putInt(SELECTED_POSITION, event.selected)
                            bundle.putSerializable(TYPE, event.type)

                            setFragmentResult(TAG, bundle)
                            dismiss()
                        }
                    }
                }
            }
        }

        return view
    }
}