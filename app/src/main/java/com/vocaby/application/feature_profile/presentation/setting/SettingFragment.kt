package com.vocaby.application.feature_profile.presentation.setting

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import com.vocaby.application.R
import com.vocaby.vocabywidgets.DescriptiveButtonView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private val settingViewModel: SettingViewModel by viewModels()
    private lateinit var notificationCollectionButton: DescriptiveButtonView
    private lateinit var notificationFrequencyButton: DescriptiveButtonView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        notificationCollectionButton = view.findViewById(R.id.notification_collection_button)
        notificationFrequencyButton = view.findViewById(R.id.notification_frequency_button)

        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }
        return view
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notificationSwitch = view.findViewById<SwitchMaterial>(R.id.notification_switch)
//        notificationSwitch.isChecked = settingViewModel.isUseConnectionEnabled()
        notificationSwitch.setOnCheckedChangeListener { _, enabled ->
            notificationCollectionButton.isEnabled = enabled
            notificationFrequencyButton.isEnabled = enabled
        }

        val connectionSwitch = view.findViewById<SwitchMaterial>(R.id.connections_switch)
        connectionSwitch.isChecked = settingViewModel.isUseConnectionEnabled()
        connectionSwitch.setOnCheckedChangeListener { _, enabled ->
            settingViewModel.setConnectionSettings(enabled)
        }

        val dataShareSwitch = view.findViewById<SwitchMaterial>(R.id.data_share_switch)
        dataShareSwitch.isChecked = settingViewModel.isDataShareEnabled()
        dataShareSwitch.setOnCheckedChangeListener { _, enabled ->
            settingViewModel.setDataShareSettings(enabled)
        }
    }
}