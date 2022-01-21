package com.vocaby.application.feature_profile.domain.use_case

class ProfileUseCases (
    val updateChartUseCase: UpdateChartUseCase,
    val updateChartModeUseCase: UpdateChartModeUseCase,
    val getChartModeUseCase: GetChartModeUseCase,
    val eraseChartDataUseCase: EraseChartDataUseCase,
    val setupUserUseCase: SetupUserUseCase
)