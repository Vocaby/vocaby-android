package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.core.domain.repository.ApplicationRepository
import com.vocaby.application.feature_dictionary.domain.model.DailyPick
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.presentation.dictionary.DailyPickState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class GetDailyPick(
    private val dictionaryRepository: DictionaryRepository,
    private val applicationRepository: ApplicationRepository,
) {
    suspend operator fun invoke(): Flow<DailyPickState> = flow {
        emit(DailyPickState.InProgress)
        val lastTimeStarted = applicationRepository.getLastTimeStarted()
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]

        if (today != lastTimeStarted) {
            val apiPick: EntryModel? = dictionaryRepository.getDailyPickFromApi()
            applicationRepository.setLastStarted(today)
            apiPick?.let {
                dictionaryRepository.cacheDailyPick(it.entry, false)
                emit(DailyPickState.Picked(DailyPick(it, random = false)))
            } ?: run {
                val randomPick = dictionaryRepository.getRandomEntry()
                dictionaryRepository.cacheDailyPick(randomPick.entry, true)
                emit(DailyPickState.Picked(DailyPick(randomPick, random = true)))
            }
        } else {
            applicationRepository.setLastStarted(today)
            val pick = dictionaryRepository.getCachedPick()
            val entryModel = dictionaryRepository.getEntryDataFromDatabase(pick.entryModel.entry)!!
            emit(DailyPickState.Picked(DailyPick(entryModel, pick.random)))
        }
    }
}