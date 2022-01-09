package com.vocaby.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.models.FeedbackModel
import com.vocaby.app.models.profile.FaqModel
import com.vocaby.app.states.GenericState
import com.vocaby.app.states.UserInputState
import com.vocaby.app.utils.Formatter
import com.vocaby.app.utils.SingleLiveEvent
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
                    val success = repository.submitFeedback(FeedbackModel(type, message, email))
                    if (success) {
                        _feedbackState.postValue(GenericState.Success(""))
                    } else {
                        _feedbackState.postValue(GenericState.Error(Exception()))
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