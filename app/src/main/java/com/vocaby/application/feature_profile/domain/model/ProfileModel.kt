package com.vocaby.application.feature_profile.domain.model

data class ProfileModel(
    val userId: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val saveCount: Int,
    val collectionCount: Int,
    val entryCount: Int,
)
