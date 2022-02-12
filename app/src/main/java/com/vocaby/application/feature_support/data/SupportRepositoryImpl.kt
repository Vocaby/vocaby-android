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
                "Vocaby's dictionary initially derived definitions from public domain sources and Wordnets. " +
                        "We are constantly maintaining and updating the dictionary so that " +
                        "you are provided with the most up-to-date definitions. " +
                        "If you would like to help improve the dictionary, please submit the form below."
            ),
            FaqModel(
                "Will definitions automatically update on my app?",
                "Yup! Once we make updates to the dictionary, your will retrieve the " +
                        "most up to date definitions on your app. You can disable this feature in the settings page."
            ),
            FaqModel(
                "Does Vocaby collect data from me?",
                "Vocaby collects data locally to provide you with statistics. " +
                        "This data is only available on your device and is not shared with anyone."
            ),
            FaqModel(
                "If I opt in to share error related data, can it be traced back to me?",
                "No, the data does not contain any personally identifiable information that can trace back to you. " +
                        "The data does contain information about the device but anything shared with us is securely encrypted."
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