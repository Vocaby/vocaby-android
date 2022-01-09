package com.vocaby.app.models

import com.google.gson.annotations.SerializedName

data class FeedbackModel(val type: String, val message: String, @SerializedName("user_email")val userEmail: String = "")
