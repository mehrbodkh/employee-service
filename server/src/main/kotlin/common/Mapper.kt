package com.mehrbod.common

import com.mehrbod.controller.model.response.PaginatedResponse
import com.mehrbod.model.ReviewDTO

fun Pair<Long, List<ReviewDTO>>.mapToPaginatedResponse(page: Int, pageSize: Int) = PaginatedResponse(
    this.second,
    PaginatedResponse.PaginationMetadata(
        page,
        pageSize,
        this.first,
        if (first == 0L) 0 else ((first + pageSize - 1) / pageSize).toInt()
    )
)