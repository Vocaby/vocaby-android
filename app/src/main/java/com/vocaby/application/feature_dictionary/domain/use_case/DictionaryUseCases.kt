package com.vocaby.application.feature_dictionary.domain.use_case

data class DictionaryUseCases(
    val getSearchHistoryUseCase: GetSearchHistoryUseCase,
    val getSearchHistoryItemUseCase: GetSearchHistoryItemUseCase,
    val validateSearchUseCase: ValidateSearchUseCase,
    val getDictionaryEntriesByCharacter: GetDictionaryEntriesByCharacter,
    val getSearchSuggestionsUseCase: GetSearchSuggestionsUseCase,
    val getDailyPick: GetDailyPick,
    val eraseSearchHistoryUserCase: EraseSearchHistoryUserCase,
    val insertSearchHistoryUseCase: InsertSearchHistoryUseCase,
    val clearDictionaryCacheUseCase: ClearDictionaryCacheUseCase
)