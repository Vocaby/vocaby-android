package com.vocaby.application.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocaby.application.data.VocabyRepository
import com.vocaby.application.models.FeedbackModel
import com.vocaby.application.models.profile.FaqModel
import com.vocaby.application.states.GenericState
import com.vocaby.application.states.UserInputState
import com.vocaby.application.states.ValidState
import com.vocaby.application.utils.Formatter
import com.vocaby.application.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SupportViewModel(val repository: VocabyRepository): ViewModel() {
    private var faqCards: List<FaqModel> = ArrayList()
    private val _faqList = SingleLiveEvent<List<FaqModel>>()
    private val _feedbackInput = SingleLiveEvent<UserInputState>()
    private val _feedbackState = SingleLiveEvent<GenericState<String>>()

    val faq get() = _faqList
    val feedbackInput get() = _feedbackInput
    val feedbackState get() = _feedbackState

    init {
        faqCards = repository.getFaq()
        _faqList.value = faqCards
    }

    fun submitFeedback(selected: String, message: String, email: String) {
        val feedbackMessage = Formatter.cleanText(message)
        if (selected.isEmpty()) {
            _feedbackInput.value = UserInputState.NoInput
        } else if (feedbackMessage.isEmpty()) {
            _feedbackInput.value = UserInputState.EmptyInput
        } else {
            if (email.isNotEmpty() && !Formatter.validateEmail(email)) {
                _feedbackInput.value = UserInputState.InvalidInput
            } else {
                _feedbackInput.postValue(UserInputState.Valid(""))
                _feedbackState.value = GenericState.InProgress
                val type: String = when (selected) {
                    "Error Report" -> "ER"
                    "Dictionary Update" -> "DI"
                    "Feature Request" -> "FE"
                    else -> ""
                }

                viewModelScope.launch {
                    when (val state = repository.submitFeedback(FeedbackModel(type, message, email))) {
                        is ValidState.Valid -> _feedbackState.postValue(GenericState.Success(""))
                        is ValidState.Error -> _feedbackState.postValue(GenericState.Error(Exception(state.data)))
                    }
                }
            }
        }
    }
}

class SupportViewModelFactory(
    private val repository: VocabyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SupportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}