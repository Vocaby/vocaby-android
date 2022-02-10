package com.vocaby.application.feature_profile.domain.use_case

class ProfileUseCases (
    val updateChartUseCase: UpdateChartUseCase,
    val getChartSettingsUseCase: GetChartSettingsUseCase,
    val eraseChartDataUseCase: EraseChartDataUseCase,
    val setupBaseUserUseCase: SetupBaseUserUseCase,
    val cleanUpUserUseCase: CleanUpUserUseCase,
    val resetUseCase: ResetUseCase
)