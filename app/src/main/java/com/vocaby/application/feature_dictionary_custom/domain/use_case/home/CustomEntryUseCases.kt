package com.vocaby.application.feature_dictionary_custom.domain.use_case.home

class CustomEntryUseCases(
    val getCustomEntriesUseCase: GetCustomEntriesUseCase,
    val filterCustomEntriesUseCase: FilterCustomEntriesUseCase,
    val getCustomEntriesCountUseCase: GetCustomEntriesCountUseCase,
    val removeCustomEntryUseCase: RemoveCustomEntryUseCase,
    val validateCustomEntryUseCase: ValidateCustomEntryUseCase,
    val removeUserEntriesUseCase: RemoveUserEntriesUseCase
)