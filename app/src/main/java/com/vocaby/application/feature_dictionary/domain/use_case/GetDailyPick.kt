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
    operator fun invoke(): Flow<DailyPickState> = flow {
        emit(DailyPickState.InProgress)

        val lastTimeStarted = applicationRepository.getLastTimeStarted()
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]
        val picks = mutableListOf<DailyPick>()

        if (today != lastTimeStarted) {
            val (api, random) = dictionaryRepository.getCachedPick()
            dictionaryRepository.cachePrevPick(api, random)
            dictionaryRepository.clearDailyPickCache()

            val apiPick = getApiPick(today)
            val randomPick = getRandomPick()
            dictionaryRepository.cacheDailyPick(apiPick?.entry ?: "", randomPick.entry)
            apiPick?.let { picks.add(it) }
            picks.add(randomPick)
        } else {
            applicationRepository.setLastStarted(today)
            val (apiPick, randomPick) = dictionaryRepository.getCachedPick()

            if (apiPick.isNotEmpty()) {
                val apiPickEntryModel = dictionaryRepository.getEntryDataFromDatabase(apiPick)
                apiPickEntryModel?.firstGroup?.let {
                    picks.add(
                        DailyPick(
                            apiPickEntryModel.entry,
                            it.type,
                            it.definitionData[0].definition,
                            it.definitionData[0].example,
                            false
                        )
                    )
                }
            }

            val randomPickEntryModel = dictionaryRepository.getEntryDataFromDatabase(randomPick)
            randomPickEntryModel?.firstGroup?.let {
                picks.add(
                    DailyPick(
                        randomPickEntryModel.entry,
                        it.type,
                        randomPickEntryModel.definitionModelWithExample!!.definition,
                        randomPickEntryModel.definitionModelWithExample!!.example,
                        true
                    )
                )
            }
        }

        emit(DailyPickState.Picked(picks))
    }

    private suspend fun getApiPick(today: Int): DailyPick? {
        val apiPick: EntryModel? = dictionaryRepository.getDailyPickFromApi()
        applicationRepository.setLastStarted(today)
        return apiPick?.let {
            return if (it.firstGroup != null) {
                DailyPick(
                    it.entry,
                    it.firstGroup!!.type,
                    it.firstGroup!!.definitionData[0].definition,
                    it.firstGroup!!.definitionData[0].example,
                    false
                )
            } else null
        }
    }

    private suspend fun getRandomPick(): DailyPick {
        val randomPick = dictionaryRepository.getRandomEntry()
        return if (randomPick.firstGroup == null) {
            getRandomPick()
        } else {
            DailyPick(
                randomPick.entry,
                randomPick.definitionModelWithExample!!.type,
                randomPick.definitionModelWithExample!!.definition,
                randomPick.definitionModelWithExample!!.example,
                true
            )
        }
    }
}