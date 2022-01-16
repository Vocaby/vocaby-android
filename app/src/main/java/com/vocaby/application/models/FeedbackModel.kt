package com.vocaby.application.models

import com.google.gson.annotations.SerializedName
import com.vocaby.application.BuildConfig

data class FeedbackModel(
    @SerializedName("feedback_type")
    val type: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("user_email")
    val userEmail: String = "",
    @SerializedName("platform")
    val platform: String = "A",
    @SerializedName("version")
    val version: String = BuildConfig.VERSION_NAME,
)
