package com.vocaby.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vocaby.app.data.VocabyRepository
import com.vocaby.app.models.profile.FaqModel
import com.vocaby.app.utils.SingleLiveEvent

class SupportViewModel(val repository: VocabyRepository): ViewModel() {
    private var faqCards: List<FaqModel> = ArrayList()
    private val _faqList = SingleLiveEvent<List<FaqModel>>()

    val faq get() = _faqList

    init {
        faqCards = repository.getFaq()
        _faqList.value = faqCards
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