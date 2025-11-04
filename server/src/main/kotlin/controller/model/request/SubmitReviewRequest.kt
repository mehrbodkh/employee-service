package com.mehrbod.controller.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SubmitReviewRequest(
    val performance: Int,
    val softSkills: Int,
    val independence: Int,
    val aspiration: Int,
) {
    init {
        require(performance in 1..10) { "Performance must be between 1 and 10" }
        require(softSkills in 1..10) { "SoftSkills must be between 1 and 10" }
        require(independence in 1..10) { "Independence must be between 1 and 10" }
        require(aspiration in 1..10) { "Aspiration must be between 1 and 10" }
    }
}
