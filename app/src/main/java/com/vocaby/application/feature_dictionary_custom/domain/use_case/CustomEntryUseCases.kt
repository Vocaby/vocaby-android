package com.vocaby.application.feature_dictionary_custom.domain.use_case

class CustomEntryUseCases(
    val getCustomEntriesUseCase: GetCustomEntriesUseCase,
    val filterCustomEntriesUseCase: FilterCustomEntriesUseCase,
    val removeCustomEntryUseCase: RemoveCustomEntryUseCase,
    val validateCustomEntryUseCase: ValidateCustomEntryUseCase,
    val removeUserEntriesUseCase: RemoveUserEntriesUseCase
)