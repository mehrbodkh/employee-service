package com.mehrbod.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDTO(
    val date: LocalDateTime,
    val performance: Int,
    val softSkills: Int,
    val independence: Int,
    val aspiration: Int,
)