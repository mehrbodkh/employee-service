package com.mehrbod.model

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeDTO(
    val name: String,
    val surname: String,
    val email: String,
    val position: String,
)
