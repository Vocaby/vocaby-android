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
                var dailyPickState: DailyPickState = DailyPickState.InProgress
                val entryModel = dictionaryRepository.getEntryDataFromDatabase(pick)

                entryModel?.firstGroup?.let {
                    dailyPickState = DailyPickState.Picked(DailyPick(
                        entryModel.entry,
                        it.type,
                        it.definitionData[0].definition,
                        it.definitionData[0].example,
                        random
                    ))
                } ?: run {
                    dailyPickState = getRandomPick()
                }

                return dailyPickState
            }
        }
    }

    private suspend fun getApiPick(today: Int): DailyPickState? {
        val apiPick: EntryModel? = dictionaryRepository.getDailyPickFromApi()
        applicationRepository.setLastStarted(today)
        return apiPick?.let {
            dictionaryRepository.cacheDailyPick(it.entry, false)
            return if (it.firstGroup != null) {
                DailyPickState.Picked(DailyPick(
                    it.entry,
                    it.firstGroup!!.type,
                    it.firstGroup!!.definitionData[0].definition,
                    it.firstGroup!!.definitionData[0].example,
                    false
                ))
            } else null
        }
    }

    private suspend fun getRandomPick(): DailyPickState {
        val randomPick = dictionaryRepository.getRandomEntry()
        return if (randomPick.firstGroup == null) {
            getRandomPick()
        } else {
            dictionaryRepository.cacheDailyPick(randomPick.entry, true)
            DailyPickState.Picked(DailyPick(
                randomPick.entry,
                randomPick.firstGroup!!.type,
                randomPick.firstGroup!!.definitionData[0].definition,
                randomPick.firstGroup!!.definitionData[0].example,
                true
            ))
        }
    }
}