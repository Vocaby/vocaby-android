package com.vocaby.application.feature_dictionary.domain.use_case

import com.vocaby.application.core.domain.repository.ApplicationRepository
import com.vocaby.application.feature_dictionary.domain.model.DailyPick
import com.vocaby.application.feature_dictionary.domain.model.EntryModel
import com.vocaby.application.feature_dictionary.domain.repository.DictionaryRepository
import java.util.*
import javax.inject.Inject

class GetDailyPick @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val applicationRepository: ApplicationRepository,
) {
    suspend operator fun invoke(): DailyPick{
        val lastTimeStarted = applicationRepository.getLastTimeStarted()
        val calendar = Calendar.getInstance()
        val today = calendar[Calendar.DAY_OF_YEAR]

        if (today != lastTimeStarted) {
            val apiPick: EntryModel? = dictionaryRepository.getDailyPickFromApi()
            applicationRepository.setLastStarted(today)
            apiPick?.let {
                dictionaryRepository.cacheDailyPick(it.entry, false)
                return DailyPick(it, random = false)
            } ?: run {
                val randomPick = dictionaryRepository.getRandomEntry()
                dictionaryRepository.cacheDailyPick(randomPick.entry, true)
                return DailyPick(randomPick, random = true)
            }
        } else {
            applicationRepository.setLastStarted(today)
            val pick = dictionaryRepository.getCachedPick()
            val entryModel = dictionaryRepository.getEntryDataFromDatabase(pick.entryModel.entry)!!
            return DailyPick(entryModel, pick.random)
        }
    }
}