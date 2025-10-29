package com.mehrbod.model

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeDTO(
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
    val position: String,
    val supervisorId: String?,
    val subordinatesCount: Int
)
