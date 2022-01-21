package com.vocaby.application.feature_support.domain.use_case

import com.vocaby.application.feature_support.domain.model.FaqModel
import com.vocaby.application.feature_support.domain.repository.SupportRepository

class GetFaqUseCase(
    private val supportRepository: SupportRepository
) {
    operator fun invoke(): List<FaqModel> = supportRepository.getFaq()
}