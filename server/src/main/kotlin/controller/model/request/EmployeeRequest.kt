package com.mehrbod.controller.model.request

import kotlinx.serialization.Serializable

@Serializable
data class EmployeeRequest(
    val name: String,
    val surname: String,
    val email: String,
    val position: String,
    val supervisorId: String? = null,
)
