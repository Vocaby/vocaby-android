package com.vocaby.application.feature_profile.presentation.setting

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.feature_profile.domain.model.NotificationFrequency
import com.vocaby.application.feature_profile.domain.model.NotificationModel
import com.vocaby.application.feature_profile.domain.use_case.SettingsUseCases
import com.vocaby.application.feature_save.domain.model.SaveCollectionModel
import com.vocaby.application.feature_save.domain.use_cases.collection.GetAllCollectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingsUseCases: SettingsUseCases,
    private val getAllCollectionsUseCase: GetAllCollectionsUseCase,
): ViewModel() {
    private var notificationCollections: List<SaveCollectionModel>? = null
    private var notificationFrequencies: List<NotificationFrequency>? = null


    private val _notificationSettings = MutableSharedFlow<NotificationModel>()
    private val _dictionarySettings = MutableSharedFlow<Boolean>()
    private val _dataSettings = MutableSharedFlow<Boolean>()
    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()

    val dataSettings get() = _dataSettings.asSharedFlow()
    val uiEvent get() = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            launch {
                settingsUseCases.getNotificationSettingsUseCase().collectLatest { model ->
                    _notificationSettings.emit(model)
                }
            }

            launch {
                val settings = settingsUseCases.getUserSettingsUseCase().first()
                val selectedCollection = settingsUseCases.getSelectedNotificationCollectionUseCase(settings.notificationCollectionId)
                val selectedFrequency  = settingsUseCases.getSelectedNotificationFrequencyUseCase(settings.notificationFrequency)

                val initSettings = SettingsUiEvent.UpdateSettings(
                    settings.notificationEnabled,
                    selectedCollection.collectionName,
                    selectedFrequency.uiText,
                    settings.updateDictionaryEnabled,
                    settings.reportErrorEnabled
                )

                _uiEvent.emit(initSettings)
            }
        }
    }

    fun changeChartMode(displayAll: Boolean) {
        viewModelScope.launch {
            settingsUseCases.updateChartModeUseCase(displayAll)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.updateNotificationSettingsUseCase(enabled)
            val initModel = settingsUseCases.getNotificationSettingsUseCase().first()
            _uiEvent.emit(SettingsUiEvent.UpdateNotification(initModel.enabled, initModel.minutes))
        }
    }

    private fun setNotificationFrequency(position: Int) {
        viewModelScope.launch {
            notificationFrequencies?.let {
                settingsUseCases.updateNotificationFrequencyUseCase(position, notificationFrequencies)
                val initModel = settingsUseCases.getNotificationSettingsUseCase().first()
                _uiEvent.emit(SettingsUiEvent.UpdateNotificationFrequency(it[position].uiText))
                _uiEvent.emit(SettingsUiEvent.UpdateNotification(initModel.enabled, initModel.minutes))
            }
        }
    }

    private fun setNotificationCollection(position: Int) {
        viewModelScope.launch {
            notificationCollections?.let {
                settingsUseCases.updateNotificationCollectionUseCase(position, notificationCollections)
                val initModel = settingsUseCases.getNotificationSettingsUseCase().first()
                _uiEvent.emit(SettingsUiEvent.UpdateNotificationCollection(it[position].collectionName))
                _uiEvent.emit(SettingsUiEvent.UpdateNotification(initModel.enabled, initModel.minutes))
            }
        }
    }

    fun setDictionaryAutoUpdateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.updateConnectionSettingsUseCase(enabled)
        }
    }

    fun setErrorReportEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.updateDataShareSettingsUseCase(enabled)
        }
    }

    fun getSaveCollections(enabled: Boolean) {
        if (enabled) {
            viewModelScope.launch {
                notificationCollections = getAllCollectionsUseCase()
                notificationCollections?.let {
                    _uiEvent.emit(SettingsUiEvent.ShowNotificationCollectionDialog(
                        title = "Select a save collection",
                        items = ArrayList(it.map { model -> model.collectionName })
                    ))
                }
            }
        }
    }

    fun getNotificationFrequencies(enabled: Boolean) {
        if (enabled) {
            viewModelScope.launch {
                notificationFrequencies = settingsUseCases.getNotificationFrequenciesUseCase()
                notificationFrequencies?.let {
                    _uiEvent.emit(SettingsUiEvent.ShowNotificationFrequencyDialog(
                        title = "Select a notification frequency",
                        items = ArrayList(it.map { model -> model.uiText })
                    ))
                }
            }
        }
    }

    fun handleDialogResult(bundle: Bundle) {
        val selectedPosition = bundle.getInt(NotificationSettingsDialogFragment.SELECTED_POSITION)
        val updateCollection = bundle.getBoolean(NotificationSettingsDialogFragment.UPDATE_COLLECTION)

        if (updateCollection) {
            setNotificationCollection(selectedPosition)
        } else {
            setNotificationFrequency(selectedPosition)
        }
    }
}