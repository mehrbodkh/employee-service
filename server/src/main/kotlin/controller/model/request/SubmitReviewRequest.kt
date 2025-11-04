package com.mehrbod.controller.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SubmitReviewRequest(
    val performance: Int,
    val softSkills: Int,
    val independence: Int,
    val aspiration: Int,
)
