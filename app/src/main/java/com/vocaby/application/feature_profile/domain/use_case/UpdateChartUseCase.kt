package com.vocaby.application.feature_profile.domain.use_case

import com.github.mikephil.charting.data.BarEntry
import com.vocaby.app.UserSettings
import com.vocaby.application.core.util.Generators
import com.vocaby.application.feature_profile.common.Constants
import com.vocaby.application.feature_profile.domain.model.ChartData
import com.vocaby.application.feature_profile.domain.model.VisitData
import com.vocaby.application.feature_profile.domain.repository.UserRepository
import com.vocaby.application.feature_profile.presentation.profile.ChartState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transform

class UpdateChartUseCase(
    private val userRepository: UserRepository,
    private val placeholderColorsList: List<Int>,
    private val colorsList: List<Int>,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    operator fun invoke(mode: UserSettings.ChartMode): Flow<ChartState> =
        userRepository.getChartData(Constants.MAX_BARS, mode)
        .transform { searchData ->
            emit(ChartState.InProgress)

            val values = arrayListOf<String>()
            val dataEntries = arrayListOf<BarEntry>()
            var maxData = VisitData("", 0)
            if (searchData.size < Constants.MIN_BARS) {
                repeat(Constants.MAX_BARS) { index ->
                    val entry = "entry ${(97 + index).toChar()}"
                    values.add(entry)
                    val count: Int = Generators.generateRandomInt(3, 10)
                    dataEntries.add(BarEntry(index.toFloat(), count.toFloat()))

                    if (maxData.count <= count) maxData = VisitData(entry, count.toLong())
                }

                emit(ChartState.Success(
                    ChartData(values, dataEntries, true),
                    placeholderColorsList,
                    maxData.entry
                ))
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

                emit(ChartState.Success(
                    ChartData(values, dataEntries, false),
                    colors,
                    maxData.entry
                ))
            }
        }.flowOn(defaultDispatcher).cancellable()
}