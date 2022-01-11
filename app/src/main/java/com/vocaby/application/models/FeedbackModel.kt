package com.vocaby.application.models

import com.google.gson.annotations.SerializedName
import com.vocaby.application.BuildConfig

data class FeedbackModel(
    @SerializedName("feedback_type")
    val type: String,
    val message: String,
    @SerializedName("user_email")
    val userEmail: String = "",
    val platform: String = "A",
    val version: String = BuildConfig.VERSION_NAME,
)
