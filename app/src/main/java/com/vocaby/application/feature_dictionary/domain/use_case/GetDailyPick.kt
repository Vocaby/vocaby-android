package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.core.domain.repository.ApplicationRepository

import com.vocaby.application.feature_dictionary.domain.model.DailyPick
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import com.vocaby.application.feature_dictionary.presentation.dictionary.DailyPickState
import java.util.*

class GetDailyPick(
    private val dictionaryRepository: DictionaryRepository,
    private val applicationRepository: ApplicationRepository,
) {
    suspend operator fun invoke(): DailyPickState {
        val lastTimeStarted = applicationRepository.getLastTimeStarted()
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]

        if (today != lastTimeStarted) {
            return getApiPick(today) ?: getRandomPick()
        } else {
            applicationRepository.setLastStarted(today)
            val (pick, random) = dictionaryRepository.getCachedPick()
            return if (pick.isEmpty()) {
                getRandomPick()
            } else {
                val entryModel = dictionaryRepository.getEntryDataFromDatabase(pick)!!
                DailyPickState.Picked(DailyPick(
                    entryModel.entry,
                    entryModel.firstGroup.type,
                    entryModel.firstGroup.definitionData[0].definition,
                    entryModel.firstGroup.definitionData[0].example,
                    random
                ))
            }
        }
    }

    private suspend fun getApiPick(today: Int): DailyPickState? {
        val apiPick: EntryModel? = dictionaryRepository.getDailyPickFromApi()
        applicationRepository.setLastStarted(today)
        return apiPick?.let {
            dictionaryRepository.cacheDailyPick(it.entry, false)
            DailyPickState.Picked(DailyPick(
                it.entry,
                it.firstGroup.type,
                it.firstGroup.definitionData[0].definition,
                it.firstGroup.definitionData[0].example,
                false
            ))
        }
    }

    private suspend fun getRandomPick(): DailyPickState {
        val randomPick = dictionaryRepository.getRandomEntry()
        dictionaryRepository.cacheDailyPick(randomPick.entry, true)
        return DailyPickState.Picked(DailyPick(
            randomPick.entry,
            randomPick.firstGroup.type,
            randomPick.firstGroup.definitionData[0].definition,
            randomPick.firstGroup.definitionData[0].example,
            true
        ))
    }
}