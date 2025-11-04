package com.mehrbod.model

import com.mehrbod.common.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class EmployeeNodeDTO(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val surname: String,
    val position: String,
    val email: String,
    @Serializable(with = UUIDSerializer::class)
    val supervisorId: UUID?,
)
