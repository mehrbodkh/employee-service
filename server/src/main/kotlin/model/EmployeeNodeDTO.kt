package com.mehrbod.model

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeNodeDTO(
    val id: String,
    val name: String,
    val surname: String,
    val position: String,
    val email: String,
    val supervisorId: String?,
)
