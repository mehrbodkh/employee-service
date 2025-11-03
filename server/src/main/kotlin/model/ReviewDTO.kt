package com.mehrbod.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDTO(
    val date: LocalDate,
    val performance: Int,
    val softSkills: Int,
    val independence: Int,
    val aspiration: Int,
)