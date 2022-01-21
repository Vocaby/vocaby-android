package com.vocaby.application.feature_profile.presentation.setting

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.vocaby.application.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private val settingViewModel: SettingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }
        return view
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectionSwitch = view.findViewById<Switch>(R.id.connections_switch)
        connectionSwitch.isChecked = settingViewModel.isUseConnectionEnabled()
        connectionSwitch.setOnCheckedChangeListener { _, enabled ->
            settingViewModel.setConnectionSettings(enabled)
        }

        val dataShareSwitch = view.findViewById<Switch>(R.id.data_share_switch)
        dataShareSwitch.isChecked = settingViewModel.isDataShareEnabled()
        dataShareSwitch.setOnCheckedChangeListener { _, enabled ->
            settingViewModel.setDataShareSettings(enabled)
        }
    }
}