package com.vocaby.application.feature_profile.presentation.setting

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import com.vocaby.application.BuildConfig
import com.vocaby.application.R
import com.vocaby.application.core.util.launchAndRepeatWithViewLifecycle
import com.vocaby.application.feature_profile.domain.model.NotificationSettings
import com.vocaby.application.feature_profile.presentation.dangerzone.DangerZoneFragment
import com.vocaby.application.feature_profile.presentation.setting.receivers.NotificationReceiver
import com.vocaby.vocabywidgets.DescriptiveButtonView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SettingFragment : Fragment() {
    private val settingsViewModel: SettingViewModel by viewModels()
    private lateinit var notificationCollectionButton: DescriptiveButtonView
    private lateinit var notificationFrequencyButton: DescriptiveButtonView
    private lateinit var notificationOptionsButton: Button
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
        notificationOptionsButton = view.findViewById(R.id.notification_options_button)

        dictionaryUpdaterSwitch = view.findViewById(R.id.connections_switch)
        dataShareSwitch = view.findViewById(R.id.data_share_switch)
        notificationSwitch = view.findViewById(R.id.notification_switch)

        setupFragmentManager()

        notificationSwitch.setOnCheckedChangeListener { _, enabled ->
            notificationCollectionButton.isEnabled = enabled
            notificationFrequencyButton.isEnabled = enabled
            notificationOptionsButton.isEnabled = enabled
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
            settingsViewModel.getSaveCollections(notificationSwitch.isEnabled, notificationCollectionButton.getDescription())
        }

        notificationFrequencyButton.setOnClickListener {
            settingsViewModel.getNotificationFrequencies(notificationSwitch.isEnabled, notificationFrequencyButton.getDescription())
        }

        notificationOptionsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
                putExtra(Settings.EXTRA_CHANNEL_ID, NotificationReceiver.CHANNEL_ID)
            }

            startActivity(intent)
        }

        // DANGER ZONE
        val dangerButton = view.findViewById<Button>(R.id.danger_zone_button)
        dangerButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.enter_right_to_left,
                    R.anim.exit_right_to_left,
                    R.anim.enter_right_to_left,
                    R.anim.exit_left_to_right
                ).add(R.id.profile_fragment_container, DangerZoneFragment())
                .addToBackStack(null)
                .commit()
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
                        val dialogFragment = NotificationSettingsDialogFragment.newInstance(event.title, event.items, event.selectedIndex, NotificationSettings.COLLECTION)
                        dialogFragment.show(childFragmentManager, NotificationSettingsDialogFragment.TAG)
                    }
                    is SettingsUiEvent.ShowNotificationFrequencyDialog -> {
                        val dialogFragment = NotificationSettingsDialogFragment.newInstance(event.title, event.items, event.selectedIndex, NotificationSettings.FREQUENCY)
                        dialogFragment.show(childFragmentManager, NotificationSettingsDialogFragment.TAG)
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
                                System.currentTimeMillis() + 1000L * 60 * event.minutes,
                                1000L * 60 * event.minutes,
                                pendingIntent
                            )

                            requireActivity().sendBroadcast(notificationIntent)
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

    private fun setupFragmentManager() {
        childFragmentManager.setFragmentResultListener(
            NotificationSettingsDialogFragment.TAG,
            this
        ) { _, bundle -> settingsViewModel.handleDialogResult(bundle) }
    }
}