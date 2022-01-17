package com.vocaby.application.feature_user.presentation.viewmodel

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.vocaby.application.core.util.Generators.generateRandomInt
import com.vocaby.application.core.util.GenericState
import com.vocaby.application.core.util.SingleLiveEvent
import com.vocaby.application.feature_user.data.local.entity.VisitData
import com.vocaby.application.feature_user.domain.model.ChartData
import com.vocaby.application.feature_user.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val repository: UserRepository
): ViewModel() {
    companion object {
        const val MAX_BARS = 5
        const val MIN_BARS = 2
    }

    private val _values = SingleLiveEvent<GenericState<Pair<ChartData, List<Int>>>>()
    private val _favoriteEntry = SingleLiveEvent<String>()
    private val _chartMode = SingleLiveEvent<Boolean>()
    private var userChartPopulated: Boolean = false

    private val placeholderColorsList: ArrayList<Int> = ArrayList()
    private val colorsList: ArrayList<Int> = ArrayList()

    val values get() = _values
    val favoriteEntry get() = _favoriteEntry
    val chartMode get() = _chartMode

    init {
        placeholderColorsList.add(Color.parseColor("#C1C1C1"))
        placeholderColorsList.add(Color.parseColor("#C8C8C8"))
        placeholderColorsList.add(Color.parseColor("#CFCFCF"))
        placeholderColorsList.add(Color.parseColor("#D6D6D6"))
        placeholderColorsList.add(Color.parseColor("#DCDCDC"))
        colorsList.add(Color.parseColor("#7BB38D"))
        colorsList.add(Color.parseColor("#89BB99"))
        colorsList.add(Color.parseColor("#A6CCB0"))
        colorsList.add(Color.parseColor("#C3DDC7"))
        colorsList.add(Color.parseColor("#D1E5D3"))
    }

    fun updateChart() {
        viewModelScope.launch(Dispatchers.Default) {
            _values.postValue(GenericState.InProgress)
            val searchData = repository.getChartData(MAX_BARS)
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

                _values.postValue(
                    GenericState.Success(Pair(
                    ChartData(values, dataEntries, true),
                    placeholderColorsList
                )))
            } else {
                val colors = mutableListOf<Int>()

                maxData = searchData[0]
                for ((index, data) in searchData.withIndex()) {
                    data.color = colorsList[index]
                }

                val sorted = searchData.sortedBy { it.entry }
                for ((index, data) in sorted.withIndex()) {
                    colors.add(data.color)
                    values.add(data.entry)
                    dataEntries.add(BarEntry(index.toFloat(), data.count.toFloat()))
                }

                userChartPopulated = true
                _values.postValue(
                    GenericState.Success(Pair(
                    ChartData(values, dataEntries, false),
                    colors
                )))
            }

            _favoriteEntry.postValue(maxData.entry)
        }
    }

    fun changeChartMode(displayAll: Boolean) {
        repository.updateChartMode(displayAll)
        _chartMode.value = displayAll
    }

    fun initializeChart() {
        _chartMode.value = repository.getChartMode()
    }

    fun eraseChartData() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = repository.getUser()
            repository.eraseVisitData(userId)
        }
    }
}