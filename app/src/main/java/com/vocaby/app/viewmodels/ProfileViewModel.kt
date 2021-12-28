package com.vocaby.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.data.entity.VisitData
import com.vocaby.app.models.profile.ChartData
import com.vocaby.app.states.GenericState
import com.vocaby.app.utils.Generators.generateRandomInt
import com.vocaby.app.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(val repository: VocabyRepository): ViewModel() {
    companion object {
        const val MAX_BARS = 5
        const val MIN_BARS = 2
    }

    private val _values = SingleLiveEvent<GenericState<ChartData>>()
    private val _favoriteEntry = SingleLiveEvent<String>()
    private var userChartPopulated: Boolean = false

    val values get() = _values
    val favoriteEntry get() = _favoriteEntry

    fun updateChart() {
        viewModelScope.launch(Dispatchers.Default) {
            _values.postValue(GenericState.InProgress)
            val searchData = repository.getWeeklyData(MAX_BARS)
            val values = arrayListOf<String>()
            val dataEntries = arrayListOf<BarEntry>()
            var maxData = VisitData("", 0)

            if (searchData.size < MIN_BARS) {
                repeat(MAX_BARS) { index ->
                    val entry = "entry ${(97 + index).toChar()}"
                    values.add(entry)
                    val count: Int = generateRandomInt(3, 10)
                    dataEntries.add(BarEntry(index.toFloat(), count.toFloat()))

                    if (maxData.count <= count) maxData = VisitData(entry, count.toLong())
                }

                _values.postValue(GenericState.Success(ChartData(values, dataEntries, true)))
            } else {
                for ((index, data) in searchData.withIndex()) {
                    values.add(data.entry)
                    dataEntries.add(BarEntry(index.toFloat(), data.count.toFloat()))

                    if (maxData.count <= data.count) maxData = data
                }

                userChartPopulated = true
                _values.postValue(GenericState.Success(ChartData(values, dataEntries, false)))
            }

            _favoriteEntry.postValue(maxData.entry)
        }
    }

    fun eraseChartData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.eraseVisitData()
        }
    }
}

class ProfileViewModelFactory(
    private val repository: VocabyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}