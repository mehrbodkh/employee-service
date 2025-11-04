package com.mehrbod.controller.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<out T>(
    val data: List<T>,
    val pagination: PaginationMetadata,
) {
    @Serializable
    data class PaginationMetadata(
        val currentPage: Int,
        val pageSize: Int,
        val totalItems: Long,
        val totalPages: Int,
    )
}


