package com.mehrbod.exception

import kotlinx.serialization.Serializable

@Serializable
data class ServerErrorMessage(
    val error: String
)
