package com.teumteumeat.teumteumeat.domain.model.on_boarding

data class UserName(
    val name: String,
)

data class OnboardingStatus(
    val completed: Boolean,
)

fun OnboardingStatus.toDomain(): OnboardingStatus =
    OnboardingStatus(
        completed = completed
    )