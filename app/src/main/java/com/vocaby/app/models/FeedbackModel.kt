package com.vocaby.app.models

import com.google.gson.annotations.SerializedName

data class FeedbackModel(@SerializedName("feedback_type")val type: String, val message: String, @SerializedName("user_email")val userEmail: String = "")
