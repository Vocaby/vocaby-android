package com.vocaby.application.feature_dictionary.domain.use_case

data class DictionaryUseCases constructor(
    val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    val validateSearchUserCase: ValidateSearchUserCase,
    val getDictionaryEntriesByCharacter: GetDictionaryEntriesByCharacter,
    val getSearchSuggestionsUseCase: GetSearchSuggestionsUseCase,
    val getDailyPick: GetDailyPick,
    val eraseSearchHistoryUserCase: EraseSearchHistoryUserCase,
    val insertSearchHistoryUseCase: InsertSearchHistoryUseCase,
    val clearDictionaryCacheUseCase: ClearDictionaryCacheUseCase
)