package com.mehrbod.model

import com.mehrbod.common.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class EmployeeDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    val name: String,
    val surname: String,
    val email: String,
    val position: String,
    @Serializable(with = UUIDSerializer::class)
    val supervisorId: UUID? = null,
    val subordinatesCount: Int? = null
)
