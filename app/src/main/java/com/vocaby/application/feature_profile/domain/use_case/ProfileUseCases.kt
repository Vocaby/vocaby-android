package com.vocaby.application.feature_profile.domain.use_case

class ProfileUseCases (
    val updateChartUseCase: UpdateChartUseCase,
    val getProfileDataUseCase: GetProfileDataUseCase,
    val getChartSettingsUseCase: GetChartSettingsUseCase,
    val eraseChartDataUseCase: EraseChartDataUseCase,
    val setupBaseUserUseCase: SetupBaseUserUseCase,
    val cleanUpUserUseCase: CleanUpUserUseCase,
    val resetUseCase: ResetUseCase
)