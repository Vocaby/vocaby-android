package com.vocaby.application.feature_support.data

import com.vocaby.application.core.util.ResourceState
import com.vocaby.application.core.util.UiText
import com.vocaby.application.feature_support.data.remote.SupportApi
import com.vocaby.application.feature_support.domain.model.FaqModel
import com.vocaby.application.feature_support.domain.model.FeedbackModel
import com.vocaby.application.feature_support.domain.repository.SupportRepository
import java.net.UnknownHostException

class SupportRepositoryImpl(
    private val supportApi: SupportApi
): SupportRepository {
    override suspend fun submitFeedback(feedbackModel: FeedbackModel): ResourceState<Nothing> {
        return try {
            val response = supportApi.submitFeedback("application/json", feedbackModel)
            if (response.isSuccessful) {
                ResourceState.Success(data = null)
            } else {
                if (response.code() >= 500) {
                    ResourceState.Error(UiText(text="Vocaby's server is down :( Please try again later"))
                } else {
                    ResourceState.Error(UiText(text="Failed to send feedback..."))
                }
            }
        } catch (e: UnknownHostException) {
            ResourceState.Error(UiText(text="No internet connection"))
        } catch (e: Throwable) {
            ResourceState.Error(UiText(text="Something went wrong..."))
        }
    }

    override fun getFaq(): List<FaqModel> {
        return listOf(
            FaqModel(
                "Is Vocaby free?",
                "Yup! Vocaby is completely free and has no hidden fees or advertisements."
            ),
            FaqModel(
                "Why are some definitions outdated?",
                "Vocaby is powered by Princeton's Wordnet. " +
                        "At Vocaby, we are maintaining and updating definitions so that " +
                        "you are provided with the most up-to-date definition. " +
                        "If you would like to help improve the dictionary, please submit the form below."
            ),
            FaqModel(
                "Will definitions automatically update on my app?",
                "Yup! Once we make updates to our dictionary, your will retrieve the " +
                        "most up to date definitions on your app. This does require an " +
                        "internet connection though."
            ),
            FaqModel(
                "Does Vocaby collect data from me?",
                "We only collect error related data to improve the app and better your experience with Vocaby. " +
                        "If you don't feel comfortable sharing this data, you can opt out in the Data Management page."
            ),
            FaqModel(
                "If I do share my data, can it be traced back to me?",
                "No, the data does not contain any personally identifiable information that can trace back to you. " +
                        "The data does contain some information about your device but anything shared with us is securely encrypted."
            ),
            FaqModel(
                "Why does my import keep failing?",
                "Please make sure that your exported backup json " +
                        "file was indeed created by the app and was not tampered with. " +
                        "If you continue to experience this issue, please feel " +
                        "free to reach out to us!"
            ),
            FaqModel(
                "Will Vocaby be available on other platforms?",
                "We intend to increase Vocaby's " +
                        "availability across platforms further down the road, but we want " +
                        "to make sure that Vocaby matures on Android first."
            )
        )
    }
}