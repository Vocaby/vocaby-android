package com.vocaby.application.feature_support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocaby.application.core.util.ResourceState
import com.vocaby.application.feature_support.domain.model.FaqModel
import com.vocaby.application.feature_support.domain.use_case.SupportUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val supportUseCases: SupportUseCases
): ViewModel() {
    private val _faqList = MutableStateFlow<List<FaqModel>>(ArrayList())
    private val _feedbackState = MutableSharedFlow<ResourceState<Nothing>>()

    val faq get() = _faqList.asStateFlow()
    val feedbackState get() = _feedbackState.asSharedFlow()

    init {
        _faqList.value = supportUseCases.getFaqUseCase()
    }

    fun submitFeedback(selected: String, message: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            supportUseCases.submitFeedbackUseCase(selected, message, email).collectLatest { state ->
                when (state) {
                    is ResourceState.InProgress -> {
                        _feedbackState.emit(state)
                    }
                    is ResourceState.Success -> {
                        _feedbackState.emit(ResourceState.Success())
                    }
                    is ResourceState.Error -> {
                        _feedbackState.emit(state)
                    }
                }
            }
        }
    }
}