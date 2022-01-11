package com.vocaby.app.models

import com.google.gson.annotations.SerializedName
import com.vocaby.app.BuildConfig

data class FeedbackModel(
    @SerializedName("feedback_type")
    val type: String,
    val message: String,
    @SerializedName("user_email")
    val userEmail: String = "",
    val platform: String = "A",
    val version: String = BuildConfig.VERSION_NAME,
)
