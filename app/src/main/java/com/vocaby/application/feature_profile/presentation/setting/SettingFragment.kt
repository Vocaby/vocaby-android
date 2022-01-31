package com.vocaby.application.feature_profile.presentation.setting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.vocaby.application.R
import com.vocaby.application.feature_profile.presentation.setting.receivers.NotificationReceiver
import com.vocaby.application.launchAndRepeatWithViewLifecycle
import com.vocaby.vocabywidgets.DescriptiveButtonView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private val settingsViewModel: SettingViewModel by viewModels()
    private lateinit var notificationCollectionButton: DescriptiveButtonView
    private lateinit var notificationFrequencyButton: DescriptiveButtonView
    private lateinit var dictionaryUpdaterSwitch: SwitchMaterial
    private lateinit var dataShareSwitch: SwitchMaterial
    private lateinit var notificationSwitch: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        notificationCollectionButton = view.findViewById(R.id.notification_collection_button)
        notificationFrequencyButton = view.findViewById(R.id.notification_frequency_button)

        dictionaryUpdaterSwitch = view.findViewById(R.id.connections_switch)
        dataShareSwitch = view.findViewById(R.id.data_share_switch)
        notificationSwitch = view.findViewById(R.id.notification_switch)

        notificationSwitch.setOnCheckedChangeListener { _, enabled ->
            notificationCollectionButton.isEnabled = enabled
            notificationFrequencyButton.isEnabled = enabled
        }

        notificationSwitch.setOnClickListener {
            settingsViewModel.setNotificationEnabled(notificationSwitch.isChecked)
        }

        dictionaryUpdaterSwitch.setOnClickListener {
            settingsViewModel.setDictionaryAutoUpdateEnabled(dictionaryUpdaterSwitch.isChecked)
        }

        dataShareSwitch.setOnClickListener {
            settingsViewModel.setErrorReportEnabled(dataShareSwitch.isChecked)
        }

        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { requireActivity().onBackPressed() }

        notificationCollectionButton.setOnClickListener {
            settingsViewModel.getSaveCollections(notificationSwitch.isEnabled)
        }

        notificationFrequencyButton.setOnClickListener {
            settingsViewModel.getNotificationFrequencies(notificationSwitch.isEnabled)
        }

        launchAndRepeatWithViewLifecycle {
            settingsViewModel.uiEvent.collect { event ->
                when(event) {
                    is SettingsUiEvent.UpdateSettings -> {
                        notificationSwitch.isChecked = event.notificationEnabled
                        notificationCollectionButton.setDescription(event.notificationCollection)
                        notificationFrequencyButton.setDescription(event.notificationFrequency)
                        dictionaryUpdaterSwitch.isChecked = event.autoUpdateEnabled
                        dataShareSwitch.isChecked = event.dataShareEnabled
                    }
                    is SettingsUiEvent.ShowNotificationCollectionDialog -> {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle(event.title)
                            .setItems(event.items) {
                                    _: DialogInterface, int: Int ->
                                notificationCollectionButton.setDescription(event.items[int])
                                settingsViewModel.setNotificationCollection(int)
                            }.show()
                    }
                    is SettingsUiEvent.ShowNotificationFrequencyDialog -> {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle(event.title)
                            .setItems(event.items) {
                                    _: DialogInterface, int: Int ->
                                notificationFrequencyButton.setDescription(event.items[int])
                                settingsViewModel.setNotificationFrequency(int)
                            }.show()
                    }
                    is SettingsUiEvent.UpdateNotificationCollection -> {
                        notificationCollectionButton.setDescription(event.collectionName)
                    }
                    is SettingsUiEvent.UpdateNotificationFrequency -> {
                        notificationFrequencyButton.setDescription(event.frequency)
                    }
                    is SettingsUiEvent.UpdateNotification -> {
                        val alarmManager = requireActivity().getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                        val notificationIntent = Intent(requireActivity(), NotificationReceiver::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(
                            requireActivity(),
                            777,
                            notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        if (event.enabled) {
                            alarmManager.setRepeating(
                                AlarmManager.RTC_WAKEUP,
                                System.currentTimeMillis(),
                                1000L * 60 * event.minutes,
                                pendingIntent
                            )
                        } else {
                            alarmManager.cancel(pendingIntent)
                            pendingIntent.cancel()
                        }
                    }
                }
            }
        }

        return view
    }
}