package com.vocaby.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.data.entity.VisitData
import com.vocaby.app.models.profile.ChartData
import com.vocaby.app.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProfileViewModel(repository: VocabyRepository): ViewModel() {
    private val _values = SingleLiveEvent<ChartData>()
    private val _favoriteEntry = SingleLiveEvent<String>()

    val values get() = _values
    val favoriteEntry get() = _favoriteEntry

    init {
        viewModelScope.launch(Dispatchers.Default) {
            repository.getWeeklyData().collect { searchData ->
                val values = arrayListOf<String>()
                val dataEntries = arrayListOf<BarEntry>()
                var maxData = VisitData("", 0)
                for ((index, data) in searchData.withIndex()) {
                    if (index > 4) break
                    values.add(data.entry)
                    dataEntries.add(BarEntry(index.toFloat(), data.count.toFloat()))
                    if (maxData.count <= data.count) maxData = data
                }


                _favoriteEntry.postValue(maxData.entry)
                _values.postValue(ChartData(values, dataEntries))
            }
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