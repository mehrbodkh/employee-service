package com.mehrbod.model

import kotlinx.serialization.Serializable

@Serializable
data class Page<out T>(
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Long,
)
